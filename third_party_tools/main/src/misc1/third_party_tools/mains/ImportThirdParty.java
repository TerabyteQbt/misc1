//   Copyright 2016 Keith Amling
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package misc1.third_party_tools.mains;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import misc1.commons.Maybe;
import misc1.commons.options.OptionsFragment;
import misc1.commons.options.OptionsLibrary;
import misc1.commons.options.OptionsResults;
import misc1.third_party_tools.ivy.IvyCache;
import misc1.third_party_tools.ivy.IvyModuleAndVersion;
import misc1.third_party_tools.ivy.PatternIvyModuleAndVersion;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qbt.HelpTier;
import qbt.NormalDependencyType;
import qbt.QbtCommand;
import qbt.QbtCommandName;
import qbt.QbtCommandOptions;
import qbt.QbtHashUtils;
import qbt.QbtTempDir;
import qbt.QbtUtils;
import qbt.config.QbtConfig;
import qbt.manifest.current.PackageManifest;
import qbt.manifest.current.PackageMetadata;
import qbt.manifest.current.QbtManifest;
import qbt.manifest.current.RepoManifest;
import qbt.options.ConfigOptionsDelegate;
import qbt.options.ManifestOptionsDelegate;
import qbt.options.ManifestOptionsResult;
import qbt.repo.LocalRepoAccessor;
import qbt.tip.PackageTip;
import qbt.tip.RepoTip;
import qbt.vcs.Repository;

public class ImportThirdParty extends QbtCommand<ImportThirdParty.Options> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportThirdParty.class);

    @QbtCommandName("importThirdParty")
    public static interface Options extends QbtCommandOptions {
        public static final OptionsLibrary<Options> o = OptionsLibrary.of();
        public final ConfigOptionsDelegate<Options> config = new ConfigOptionsDelegate<Options>();
        public final ManifestOptionsDelegate<Options> manifest = new ManifestOptionsDelegate<Options>();
        public static final OptionsFragment<Options, String> destinationRepo = o.oneArg("repo").transform(o.singleton("3p")).helpDesc("Repository to place the modules in when they don't already exist");
    }

    @Override
    public String getDescription() {
        return "Import third party modules from ivy/maven sources for use within QBT";
    }

    @Override
    public HelpTier getHelpTier() {
        return HelpTier.ARCANE;
    }

    @Override
    public Class<Options> getOptionsClass() {
        return Options.class;
    }

    @Override
    public int run(OptionsResults<? extends Options> options) throws Exception {
        QbtConfig config = Options.config.getConfig(options);
        ManifestOptionsResult manifestResult = Options.manifest.getResult(options);
        QbtManifest manifest = manifestResult.parse();

        String repo = options.get(Options.destinationRepo);
        RepoTip destinationRepo = RepoTip.TYPE.parseRequire(repo);

        // ensure we have an override for the given repo
        final LocalRepoAccessor localRepoAccessor = config.localRepoFinder.findLocalRepo(destinationRepo);
        if(localRepoAccessor == null) {
            throw new IllegalArgumentException("You must have a local override of repo " + destinationRepo);
        }
        Path repoRoot = localRepoAccessor.dir.normalize();
        Repository vcsRepo = localRepoAccessor.vcs.getRepository(repoRoot);
        IvyCache ivyCache = new IvyCache(repoRoot.resolve("mc/.cache"));

        Path configFile = repoRoot.resolve("mc/.config");
        ImmutableList.Builder<IvyModuleAndVersion> modulesBuilder = ImmutableList.builder();
        ImmutableList.Builder<Pair<PatternIvyModuleAndVersion, IvyModuleAndVersion>> addDependencyBuilder = ImmutableList.builder();
        ImmutableList.Builder<Pair<PatternIvyModuleAndVersion, PatternIvyModuleAndVersion>> removeDependencyBuilder = ImmutableList.builder();
        ImmutableList.Builder<Triple<PatternIvyModuleAndVersion, PatternIvyModuleAndVersion, IvyModuleAndVersion>> rewriteDependencyBuilder = ImmutableList.builder();
        ImmutableListMultimap.Builder<String, String> linkCheckerArgsBuilder = ImmutableListMultimap.builder();
        Pattern modulePattern = Pattern.compile("^MODULE:(.*)$");
        Pattern addPattern = Pattern.compile("^ADD:(.*),(.*)$");
        Pattern removePattern = Pattern.compile("^REMOVE:(.*),(.*)$");
        Pattern rewritePattern = Pattern.compile("^REWRITE:(.*),(.*),(.*)$");
        Pattern linkCheckerArgsPattern = Pattern.compile("^LINK_CHECKER_ARGS:(.*),(.*)$");
        for(String configLine : QbtUtils.readLines(configFile)) {
            if(configLine.isEmpty()) {
                continue;
            }
            if(configLine.startsWith("#")) {
                continue;
            }

            Matcher moduleMatcher = modulePattern.matcher(configLine);
            if(moduleMatcher.matches()) {
                modulesBuilder.add(new IvyModuleAndVersion(moduleMatcher.group(1)));
                continue;
            }
            Matcher addMatcher = addPattern.matcher(configLine);
            if(addMatcher.matches()) {
                addDependencyBuilder.add(Pair.of(new PatternIvyModuleAndVersion(addMatcher.group(1)), new IvyModuleAndVersion(addMatcher.group(2))));
                continue;
            }
            Matcher removeMatcher = removePattern.matcher(configLine);
            if(removeMatcher.matches()) {
                removeDependencyBuilder.add(Pair.of(new PatternIvyModuleAndVersion(removeMatcher.group(1)), new PatternIvyModuleAndVersion(removeMatcher.group(2))));
                continue;
            }
            Matcher rewriteMatcher = rewritePattern.matcher(configLine);
            if(rewriteMatcher.matches()) {
                rewriteDependencyBuilder.add(Triple.of(new PatternIvyModuleAndVersion(rewriteMatcher.group(1)), new PatternIvyModuleAndVersion(rewriteMatcher.group(2)), new IvyModuleAndVersion(rewriteMatcher.group(3))));
                continue;
            }
            Matcher linkCheckerArgsMatcher = linkCheckerArgsPattern.matcher(configLine);
            if(linkCheckerArgsMatcher.matches()) {
                linkCheckerArgsBuilder.put(linkCheckerArgsMatcher.group(1), linkCheckerArgsMatcher.group(2));
                continue;
            }

            throw new IllegalArgumentException("Bad config line: " + configLine);
        }

        ImmutableList<IvyModuleAndVersion> modules = modulesBuilder.build();
        ImmutableList<Pair<PatternIvyModuleAndVersion, IvyModuleAndVersion>> addDependency = addDependencyBuilder.build();
        ImmutableList<Pair<PatternIvyModuleAndVersion, PatternIvyModuleAndVersion>> removeDependency = removeDependencyBuilder.build();
        ImmutableList<Triple<PatternIvyModuleAndVersion, PatternIvyModuleAndVersion, IvyModuleAndVersion>> rewriteDependency = rewriteDependencyBuilder.build();
        ImmutableListMultimap<String, String> linkCheckerArgs = linkCheckerArgsBuilder.build();

        LOGGER.info("Calculating Maximal Versions");
        // this map contains String => maxVersion, moduledescriptor, set of package tip dependencies (of that version)
        Map<String, Triple<ComparableVersion, IvyModuleAndVersion, Set<String>>> maximalVersions = Maps.newHashMap();
        {
            Set<String> checkedDisk = Sets.newHashSet();
            Queue<IvyModuleAndVersion> queue = Lists.newLinkedList(modules);
            Set<IvyModuleAndVersion> queued = Sets.newHashSet(queue);

            while(!queue.isEmpty()) {
                IvyModuleAndVersion module = queue.poll();
                // Calculate the ivy version
                if(module.version.equals("0")) {
                    continue;
                }
                ComparableVersion requestedCv = new ComparableVersion(module.version);
                String packageName = getPackageFromModule(module);
                String packagePath = getPackagePathFromModule(module);
                Path newPackagePath = repoRoot.resolve(packagePath).normalize();

                if(checkedDisk.add(packageName)) {
                    // get version on disk if we have it?
                    if(manifest.packageToRepo.containsKey(packageName)) {
                        LOGGER.debug("Looking for existing version for module \"" + module + "\"");
                        // get it!
                        Path metadataPath = newPackagePath.resolve("maven-module.info");
                        if(!Files.exists(metadataPath)) {
                            throw new IllegalStateException("Package " + packageName + " is already in the manifest but has no maven-module.info file");
                        }
                        IvyModuleAndVersion oldModule = new IvyModuleAndVersion(QbtUtils.readLines(metadataPath).get(0).replaceFirst("Maven Module Version ",  ""));
                        LOGGER.debug("Found module \"" + oldModule + "\" on disk");
                        if(queued.add(oldModule)) {
                            queue.add(oldModule);
                        }
                    }
                }

                // Hacksaw dependencies according to very scary options
                ImmutableSet.Builder<IvyModuleAndVersion> mungedDependencies = ImmutableSet.builder();
                for(IvyModuleAndVersion depModule : ivyCache.queryIvy(module).getDependencies()) {
                    LOGGER.debug("Unmunged dependency: " + module + " -> " + depModule);
                    boolean remove = false;
                    for(Pair<PatternIvyModuleAndVersion, PatternIvyModuleAndVersion> removeEntry : removeDependency) {
                        if(removeEntry.getLeft().matches(module) && removeEntry.getRight().matches(depModule)) {
                            remove = true;
                            break;
                        }
                    }
                    if(remove) {
                        continue;
                    }

                    IvyModuleAndVersion rewritten = null;
                    for(Triple<PatternIvyModuleAndVersion, PatternIvyModuleAndVersion, IvyModuleAndVersion> rewriteEntry : rewriteDependency) {
                        if(rewriteEntry.getLeft().matches(module) && rewriteEntry.getMiddle().matches(depModule)) {
                            rewritten = rewriteEntry.getRight();
                            break;
                        }
                    }
                    if(rewritten != null) {
                        mungedDependencies.add(rewritten);
                        continue;
                    }

                    mungedDependencies.add(depModule);
                }
                for(Pair<PatternIvyModuleAndVersion, IvyModuleAndVersion> addEntry : addDependency) {
                    if(addEntry.getLeft().matches(module)) {
                        mungedDependencies.add(addEntry.getRight());
                    }
                }

                ImmutableSet.Builder<String> depsBuilder = ImmutableSet.builder();
                for(IvyModuleAndVersion depModule : mungedDependencies.build()) {
                    LOGGER.debug("Munged dependency: " + module + " -> " + depModule);
                    if(queued.add(depModule)) {
                        queue.add(depModule);
                    }
                    depsBuilder.add(getPackageFromModule(depModule));
                }
                Set<String> deps = depsBuilder.build();

                Triple<ComparableVersion, IvyModuleAndVersion, Set<String>> alreadyTriple = maximalVersions.get(packageName);
                boolean keep = false;
                if(alreadyTriple != null) {
                    ComparableVersion oldCv = alreadyTriple.getLeft();
                    if(requestedCv.compareTo(oldCv) <= 0) {
                        LOGGER.debug("[" + packageName + "] Already have version " + oldCv + " which is not older than requested version " + requestedCv + ", keeping existing version");
                    }
                    else {
                        LOGGER.debug("[" + packageName + "] Already have version " + oldCv + " is older than requested version " + requestedCv + ", replacing");
                        keep = true;
                    }
                }
                else {
                    LOGGER.debug("[" + packageName + "] First version is " + requestedCv + ", adding");
                    keep = true;
                }
                if(keep) {
                    maximalVersions.put(packageName, Triple.of(requestedCv, module, deps));
                }
            }
        }

        ImmutableMap.Builder<String, IvyModuleAndVersion> installsBuilder = ImmutableMap.builder();
        Multimap<String, String> dependencyEdges = HashMultimap.create();
        {
            Queue<String> queue = Lists.newLinkedList();
            Set<String> queued = Sets.newHashSet();
            for(IvyModuleAndVersion module : modules) {
                String packageName = getPackageFromModule(module);
                if(queued.add(packageName)) {
                    queue.add(packageName);
                }
            }
            while(!queue.isEmpty()) {
                String packageName = queue.poll();
                Triple<ComparableVersion, IvyModuleAndVersion, Set<String>> maximalVersion = maximalVersions.get(packageName);
                installsBuilder.put(packageName, maximalVersion.getMiddle());
                for(String depName : maximalVersion.getRight()) {
                    dependencyEdges.put(packageName, depName);
                    if(queued.add(depName)) {
                        queue.add(depName);
                    }
                }
            }
        }

        LOGGER.info("Fetching/Upgrading packages");
        // We've now have a complete list of maximal versions.
        QbtManifest.Builder newManifest = manifest.builder();
        boolean changed = false;
        for(Map.Entry<String, IvyModuleAndVersion> install : installsBuilder.build().entrySet()) {
            String packageName = install.getKey();
            IvyModuleAndVersion module = install.getValue();
            String packagePath = getPackagePathFromModule(module);

            Path repoDir = localRepoAccessor.dir;
            Path newPackagePath = repoDir.resolve(packagePath).normalize();
            if(Files.exists(newPackagePath)) {
                // make sure newPackagePath is actually still inside repoDir
                repoDir.relativize(newPackagePath);

                LOGGER.debug("Upgrading existing package " + packageName + " using module string " + module);
                QbtUtils.deleteRecursively(newPackagePath, false);
            }
            else {
                LOGGER.debug("Fetching new package " + packageName + " using module string " + module);
            }

            // Create the package
            QbtUtils.mkdirs(newPackagePath);

            // put in qbt-make
            Path qbtMakePath = newPackagePath.resolve("qbt-make");
            QbtUtils.writeLines(qbtMakePath, createQbtMakeContents(linkCheckerArgs.get(packageName)));
            qbtMakePath.toFile().setExecutable(true);

            downloadIvyModule(ivyCache, module, newPackagePath);

            newManifest = newManifest.transform(destinationRepo, (rmb) -> {
                return rmb.transform(RepoManifest.PACKAGES, (packages) -> {
                    packages = packages.with(packageName, createPackageManifest(destinationRepo, dependencyEdges, module, packageName, false));
                    packages = packages.with(packageName + ".lc", createPackageManifest(destinationRepo, dependencyEdges, module, packageName, true));
                    return packages;
                });
            });
            changed = true;
        }

        if(!changed) {
            LOGGER.info("No packages were modified, nothing to do");
            return 0;
        }
        // Update the manifest
        manifestResult.deparse(newManifest.build());

        return 0;
    }

    private PackageManifest.Builder createPackageManifest(RepoTip destinationRepo, Multimap<String, String> dependencyEdges, IvyModuleAndVersion module, String packageName, boolean isLinkCheck) {
        String packagePath = getPackagePathFromModule(module);
        LOGGER.debug("Building package manifest for module " + module + " (" + packageName + ")");
        PackageManifest.Builder pmb = PackageManifest.TYPE.builder();
        pmb = pmb.transform(PackageManifest.METADATA, (metadata) -> metadata.set(PackageMetadata.ARCH_INDEPENDENT, true));
        pmb = pmb.transform(PackageManifest.METADATA, (metadata) -> metadata.set(PackageMetadata.PREFIX, Maybe.of(packagePath)));
        pmb = pmb.transform(PackageManifest.METADATA, (metadata) -> metadata.set(PackageMetadata.QBT_ENV, ImmutableMap.of("JDK", Maybe.of("1_8"))));

        // add deps
        for(String dep : dependencyEdges.get(packageName)) {
            LOGGER.debug("Package " + packageName + " depends upon " + dep);
            pmb = pmb.transform(PackageManifest.NORMAL_DEPS, (normalDeps) -> normalDeps.with(dep, Pair.of(NormalDependencyType.STRONG, destinationRepo.tip)));
        }
        if(isLinkCheck) {
            // add link checker by default - our default script uses it
            pmb = pmb.transform(PackageManifest.NORMAL_DEPS, (normalDeps) -> normalDeps.with("qbt_fringe.link_checker.release", Pair.of(NormalDependencyType.BUILDTIME_WEAK, "HEAD")));
        }
        else {
            pmb = pmb.transform(PackageManifest.VERIFY_DEPS, (verifyDeps) -> verifyDeps.with(Pair.of(PackageTip.TYPE.of(packageName + ".lc", destinationRepo.tip), "link_check"), ObjectUtils.NULL));
        }

        return pmb;
    }

    private static ImmutableList<String> getMetadata(IvyModuleAndVersion module, final Path newPackagePath) throws IOException {

        final ImmutableList.Builder<String> lines = ImmutableList.builder();
        lines.add("Maven Module Version " + module);
        Path srcRoot = newPackagePath.resolve("src");
        if(!Files.exists(srcRoot)) {
            // module had no artifacts
            return lines.build();
        }
        final TreeMap<String, HashCode> hashes = Maps.newTreeMap();
        Files.walkFileTree(srcRoot, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = newPackagePath.relativize(file);
                hashes.put(relativePath.toString(), QbtHashUtils.hash(file));
                return FileVisitResult.CONTINUE;
            }
        });
        for(Map.Entry<String, HashCode> e : hashes.entrySet()) {
            lines.add(e.getValue() + " " + e.getKey());
        }
        return lines.build();
    }

    public static void downloadIvyModule(IvyCache ivyCache, IvyModuleAndVersion module, Path destinationPath) throws Exception {
        try(QbtTempDir tempDir = new QbtTempDir()) {
            ImmutableList<Pair<Path, Path>> filePairs = ivyCache.queryIvy(module).getFiles();
            for(Pair<Path, Path> filePair : filePairs) {
                Path source = filePair.getRight();
                Path dest = destinationPath.resolve("src").resolve(filePair.getLeft());
                Files.createDirectories(dest.getParent());
                Files.copy(source, dest);
            }

            if(filePairs.isEmpty()) {
                LOGGER.warn("Module " + module + " has no artifacts");
                // file is "gitkeep" not ".gitkeep" so it also prevents "cp src/*" from failing package build.
                QbtUtils.mkdirs(destinationPath.resolve("src"));
                QbtUtils.writeLines(destinationPath.resolve("src/gitkeep"), ImmutableList.of("This package contains no artifacts"));
            }

            // put in metadata
            QbtUtils.writeLines(destinationPath.resolve("maven-module.info"), getMetadata(module, destinationPath));
        }
    }

    private static String getPackageFromModule(IvyModuleAndVersion module) {
        return ("mc." + module.group + "." + module.module).replaceAll("-", ".");
    }
    private static String getPackagePathFromModule(IvyModuleAndVersion module) {
        return "mc" + File.separator + module.group + File.separator + module.module;
    }

    private static ImmutableList<String> createQbtMakeContents(ImmutableList<String> linkCheckerArgs) {
        ImmutableList.Builder<String> b = ImmutableList.builder();
        b.add("#!/bin/bash");
        b.add("");
        b.add("# This file generated by the third party importer tool");
        b.add("");
        b.add("eval export JAVA_HOME=\\$JAVA_${QBT_ENV_JDK}_HOME");
        b.add("");
        b.add("set -e");
        b.add("cp -R $PACKAGE_DIR/src/* $OUTPUT_ARTIFACTS_DIR/");
        b.add("if [ -d $INPUT_ARTIFACTS_DIR/weak/qbt_fringe.link_checker.release ]");
        b.add("then");
        b.add("    $INPUT_ARTIFACTS_DIR/weak/qbt_fringe.link_checker.release/strong/qbt_fringe.link_checker.release/bin/link_checker --qbtDefaults " + Joiner.on(' ').join(linkCheckerArgs));
        b.add("fi");
        return b.build();
    }
}
