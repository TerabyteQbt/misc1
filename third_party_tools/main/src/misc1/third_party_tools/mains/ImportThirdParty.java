package misc1.third_party_tools.mains;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import misc1.commons.ExceptionUtils;
import misc1.commons.Maybe;
import misc1.commons.options.OptionsFragment;
import misc1.commons.options.OptionsLibrary;
import misc1.commons.options.OptionsResults;
import misc1.third_party_tools.ivy.IvyCache;
import misc1.third_party_tools.ivy.IvyModule;
import misc1.third_party_tools.ivy.IvyModuleAndVersion;
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
import qbt.manifest.PackageBuildType;
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
        QbtManifest manifest = manifestResult.parse(config.manifestParser);

        String repo = options.get(Options.destinationRepo);
        RepoTip destinationRepo = RepoTip.TYPE.parseRequire(repo);

        // ensure we have an override for the given repo
        final LocalRepoAccessor localRepoAccessor = config.localRepoFinder.findLocalRepo(destinationRepo);
        if(localRepoAccessor == null) {
            throw new IllegalArgumentException("You must have a local override of repo " + destinationRepo);
        }
        Path repoRoot = localRepoAccessor.dir.normalize();
        IvyCache ivyCache = new IvyCache(repoRoot.resolve("mc/cache"));

        // Step 0: read config
        ImmutableList<IvyModuleAndVersion> modules;
        ImmutableList<Pair<IvyModuleAndVersion, IvyModuleAndVersion>> addDependency;
        ImmutableList<Pair<IvyModuleAndVersion, IvyModuleAndVersion>> removeDependency;
        ImmutableList<Triple<IvyModuleAndVersion, IvyModuleAndVersion, IvyModuleAndVersion>> rewriteDependency;
        ImmutableListMultimap<String, String> linkCheckerArgs;
        {
            ImmutableList.Builder<IvyModuleAndVersion> modulesBuilder = ImmutableList.builder();
            ImmutableList.Builder<Pair<IvyModuleAndVersion, IvyModuleAndVersion>> addDependencyBuilder = ImmutableList.builder();
            ImmutableList.Builder<Pair<IvyModuleAndVersion, IvyModuleAndVersion>> removeDependencyBuilder = ImmutableList.builder();
            ImmutableList.Builder<Triple<IvyModuleAndVersion, IvyModuleAndVersion, IvyModuleAndVersion>> rewriteDependencyBuilder = ImmutableList.builder();
            ImmutableListMultimap.Builder<String, String> linkCheckerArgsBuilder = ImmutableListMultimap.builder();
            Pattern modulePattern = Pattern.compile("^MODULE:(.*)$");
            Pattern addPattern = Pattern.compile("^ADD:(.*),(.*)$");
            Pattern removePattern = Pattern.compile("^REMOVE:(.*),(.*)$");
            Pattern rewritePattern = Pattern.compile("^REWRITE:(.*),(.*),(.*)$");
            Pattern linkCheckerArgsPattern = Pattern.compile("^LINK_CHECKER_ARGS:(.*),(.*)$");
            for(String configLine : QbtUtils.readLines(repoRoot.resolve("mc/config"))) {
                if(configLine.isEmpty()) {
                    continue;
                }
                if(configLine.startsWith("#")) {
                    continue;
                }

                Matcher moduleMatcher = modulePattern.matcher(configLine);
                if(moduleMatcher.matches()) {
                    modulesBuilder.add(IvyModuleAndVersion.parse(moduleMatcher.group(1)));
                    continue;
                }
                Matcher addMatcher = addPattern.matcher(configLine);
                if(addMatcher.matches()) {
                    addDependencyBuilder.add(Pair.of(IvyModuleAndVersion.parse(addMatcher.group(1)), IvyModuleAndVersion.parse(addMatcher.group(2))));
                    continue;
                }
                Matcher removeMatcher = removePattern.matcher(configLine);
                if(removeMatcher.matches()) {
                    removeDependencyBuilder.add(Pair.of(IvyModuleAndVersion.parse(removeMatcher.group(1)), IvyModuleAndVersion.parse(removeMatcher.group(2))));
                    continue;
                }
                Matcher rewriteMatcher = rewritePattern.matcher(configLine);
                if(rewriteMatcher.matches()) {
                    rewriteDependencyBuilder.add(Triple.of(IvyModuleAndVersion.parse(rewriteMatcher.group(1)), IvyModuleAndVersion.parse(rewriteMatcher.group(2)), IvyModuleAndVersion.parse(rewriteMatcher.group(3))));
                    continue;
                }
                Matcher linkCheckerArgsMatcher = linkCheckerArgsPattern.matcher(configLine);
                if(linkCheckerArgsMatcher.matches()) {
                    linkCheckerArgsBuilder.put(linkCheckerArgsMatcher.group(1), linkCheckerArgsMatcher.group(2));
                    continue;
                }

                throw new IllegalArgumentException("Bad config line: " + configLine);
            }
            modules = modulesBuilder.build();
            addDependency = addDependencyBuilder.build();
            removeDependency = removeDependencyBuilder.build();
            rewriteDependency = rewriteDependencyBuilder.build();
            linkCheckerArgs = linkCheckerArgsBuilder.build();
        }

        // Step 1: compute multimap of direct dependencies, as rewritten by
        // horrifying configuration.
        ImmutableMultimap<IvyModuleAndVersion, IvyModuleAndVersion> directDependencies;
        {
            ImmutableMultimap.Builder<IvyModuleAndVersion, IvyModuleAndVersion> directDependenciesBuilder = ImmutableMultimap.builder();
            Deque<IvyModuleAndVersion> queue = Lists.newLinkedList(modules);
            Set<IvyModuleAndVersion> enqueued = Sets.newHashSet(modules);
            while(!queue.isEmpty()) {
                IvyModuleAndVersion from = queue.removeFirst();


                ImmutableSet.Builder<IvyModuleAndVersion> tosBuilder = ImmutableSet.builder();
                for(IvyModuleAndVersion to : ivyCache.queryIvy(from).getDependencies()) {
                    LOGGER.debug("Unmunged dependency: " + from + " -> " + to);
                    boolean remove = false;
                    for(Pair<IvyModuleAndVersion, IvyModuleAndVersion> removeEntry : removeDependency) {
                        if(removeEntry.getLeft().contains(from) && removeEntry.getRight().contains(to)) {
                            remove = true;
                            break;
                        }
                    }
                    if(remove) {
                        LOGGER.debug("Removed dependency: " + from + " -> " + to);
                        continue;
                    }

                    IvyModuleAndVersion rewritten = null;
                    for(Triple<IvyModuleAndVersion, IvyModuleAndVersion, IvyModuleAndVersion> rewriteEntry : rewriteDependency) {
                        if(rewriteEntry.getLeft().contains(from) && rewriteEntry.getMiddle().contains(to)) {
                            rewritten = rewriteEntry.getRight();
                            break;
                        }
                    }
                    if(rewritten != null) {
                        LOGGER.debug("Rewritten dependency: " + from + " -> " + to + " -> " + rewritten);
                        tosBuilder.add(rewritten);
                        continue;
                    }

                    tosBuilder.add(to);
                }
                for(Pair<IvyModuleAndVersion, IvyModuleAndVersion> addEntry : addDependency) {
                    if(addEntry.getLeft().contains(from)) {
                        IvyModuleAndVersion to = addEntry.getRight();
                        LOGGER.debug("Added dependency: " + from + " -> " + to);
                        tosBuilder.add(to);
                    }
                }

                for(IvyModuleAndVersion to : tosBuilder.build()) {
                    directDependenciesBuilder.put(from, to);
                    if(!to.version.equals("0") && enqueued.add(to)) {
                        queue.add(to);
                    }
                }
            }
            directDependencies = directDependenciesBuilder.build();
        }

        // Step 2a: flatten module+group+version direct deps into group+module direct deps
        ImmutableMultimap<IvyModule, IvyModule> directVersionlessDependencies;
        {
            ImmutableMultimap.Builder<IvyModule, IvyModule> directVersionlessDependenciesBuilder = ImmutableMultimap.builder();
            for(Map.Entry<IvyModuleAndVersion, IvyModuleAndVersion> e : directDependencies.entries()) {
                IvyModuleAndVersion from = e.getKey();
                IvyModuleAndVersion to = e.getValue();
                directVersionlessDependenciesBuilder.put(from.withoutVersion(), to.withoutVersion());
            }
            directVersionlessDependencies = directVersionlessDependenciesBuilder.build();
        }

        // Step 2b: compute topo order of group+module pairs to process
        ImmutableList<IvyModule> order;
        {
            ImmutableList.Builder<IvyModule> orderBuilder = ImmutableList.builder();
            Set<IvyModule> built = Sets.newHashSet();
            Set<IvyModule> building = Sets.newHashSet();
            class Helper {
                void build(IvyModule module) {
                    if(built.contains(module)) {
                        return;
                    }
                    if(!building.add(module)) {
                        throw new IllegalStateException("Cycle in group+module dependency graph at " + module + "!");
                    }
                    // build children first
                    for(IvyModule module2 : directVersionlessDependencies.get(module)) {
                        build(module2);
                    }
                    // and ourselves only after all our children
                    orderBuilder.add(module);
                    building.remove(module);
                    built.add(module);
                }
            }
            Helper h = new Helper();
            for(IvyModuleAndVersion module : modules) {
                h.build(module.withoutVersion());
            }
            // the twist: we have to process in reverse (top down)
            order = orderBuilder.build().reverse();
        }

        // Step 3: walk the topo order, upgrading dependency versions as we go
        ImmutableList<IvyModuleAndVersion> installs;
        {
            ImmutableList.Builder<IvyModuleAndVersion> installsBuilder = ImmutableList.builder();
            Map<IvyModule, String> tempVersions = Maps.newHashMap();
            class Helper {
                void upgrade(String from, IvyModuleAndVersion module) {
                    if(module.version.equals("0")) {
                        // nope, someone else had better specify a version
                        return;
                    }
                    IvyModule key = module.withoutVersion();
                    String already = tempVersions.get(key);
                    final String desc;
                    if(already == null) {
                        desc = "first";
                        tempVersions.put(key, module.version);
                    }
                    else if(compareVersions(module.version, already) >= 0) {
                        desc = "upgrades from " + already;
                        tempVersions.put(key, module.version);
                    }
                    else {
                        desc = "refuse downgrade from " + already;
                    }
                    LOGGER.debug("Computing installs: upgrade: " + from + " -> " + key + " -> " + module.version + " (" + desc + ")");
                }
            }
            Helper h = new Helper();
            for(IvyModuleAndVersion module : modules) {
                h.upgrade("(requested)", module);
            }
            for(IvyModule module : order) {
                String version = tempVersions.get(module);
                if(version == null) {
                    // module had been used at some point but upgrades pruned
                    // it somewhere along the line.
                    LOGGER.debug("Computing installs: dumped: " + module);
                    continue;
                }
                IvyModuleAndVersion install = module.withVersion(version);
                LOGGER.debug("Computing installs: accepted: " + install);
                installsBuilder.add(install);
                for(IvyModuleAndVersion dep : directDependencies.get(install)) {
                    h.upgrade(install.toString(), dep);
                }
            }
            installs = installsBuilder.build();
        }

        Path repoDir = localRepoAccessor.dir;
        QbtUtils.deleteRecursively(repoDir.resolve(getRootPackagePath()), false);

        LOGGER.info("Fetching packages");
        QbtManifest.Builder newManifest = manifest.builder();
        newManifest = newManifest.transform(destinationRepo, (rmb) -> {
            return rmb.transform(RepoManifest.PACKAGES, (packages) -> {
                for(IvyModuleAndVersion module : installs) {
                    String packageName = getPackageName(module);
                    String packagePath = getPackagePath(module);

                    Path newPackagePath = repoDir.resolve(packagePath).normalize();
                    LOGGER.debug("Fetching package " + packageName + " into " + newPackagePath + " using module string " + module);

                    // Create the package
                    QbtUtils.mkdirs(newPackagePath);

                    // put in qbt-make
                    Path qbtMakePath = newPackagePath.resolve("lc/qbt-make");
                    QbtUtils.mkdirs(qbtMakePath.getParent());
                    QbtUtils.writeLines(qbtMakePath, createLcQbtMakeContents(packageName, linkCheckerArgs.get(packageName)));
                    qbtMakePath.toFile().setExecutable(true);

                    downloadIvyModule(ivyCache, module, newPackagePath);

                    packages = packages.with(packageName, createPackageManifest(destinationRepo, directDependencies.get(module), module));
                    packages = packages.with(packageName + ".lc", createLcPackageManifest(destinationRepo, module));
                }
                return packages;
            });
        });

        // Update the manifest
        manifestResult.deparse(config.manifestParser, newManifest.build());

        return 0;
    }

    private PackageManifest.Builder createPackageManifest(RepoTip destinationRepo, Collection<IvyModuleAndVersion> directDependencies, IvyModuleAndVersion module) {
        String packageName = getPackageName(module);
        LOGGER.debug("Building package manifest for module " + module + " (" + packageName + ")");
        PackageManifest.Builder pmb = PackageManifest.TYPE.builder();
        pmb = pmb.transform(PackageManifest.METADATA, (metadata) -> metadata.set(PackageMetadata.ARCH_INDEPENDENT, true));
        pmb = pmb.transform(PackageManifest.METADATA, (metadata) -> metadata.set(PackageMetadata.PREFIX, getPackagePath(module) + "/src"));
        pmb = pmb.transform(PackageManifest.METADATA, (metadata) -> metadata.set(PackageMetadata.BUILD_TYPE, PackageBuildType.COPY));

        // add deps
        for(IvyModuleAndVersion dep : directDependencies) {
            String depPackageName = getPackageName(dep);
            LOGGER.debug("Package " + packageName + " depends upon " + dep + " (" + depPackageName + ")");
            pmb = pmb.transform(PackageManifest.NORMAL_DEPS, (normalDeps) -> normalDeps.with(depPackageName, Pair.of(NormalDependencyType.STRONG, destinationRepo.tip)));
        }
        pmb = pmb.transform(PackageManifest.VERIFY_DEPS, (verifyDeps) -> verifyDeps.with(Pair.of(PackageTip.TYPE.of(packageName + ".lc", destinationRepo.tip), "link_check"), ObjectUtils.NULL));

        return pmb;
    }

    private PackageManifest.Builder createLcPackageManifest(RepoTip destinationRepo, IvyModuleAndVersion module) {
        String packageName = getPackageName(module);
        PackageManifest.Builder pmb = PackageManifest.TYPE.builder();
        pmb = pmb.transform(PackageManifest.METADATA, (metadata) -> metadata.set(PackageMetadata.ARCH_INDEPENDENT, true));
        pmb = pmb.transform(PackageManifest.METADATA, (metadata) -> metadata.set(PackageMetadata.PREFIX, getPackagePath(module) + "/lc"));
        pmb = pmb.transform(PackageManifest.METADATA, (metadata) -> metadata.set(PackageMetadata.QBT_ENV, ImmutableMap.of("JDK", Maybe.of("1_8"))));
        pmb = pmb.transform(PackageManifest.NORMAL_DEPS, (normalDeps) -> normalDeps.with(packageName, Pair.of(NormalDependencyType.BUILDTIME_WEAK, destinationRepo.tip)));
        pmb = pmb.transform(PackageManifest.NORMAL_DEPS, (normalDeps) -> normalDeps.with("qbt_fringe.link_checker.release", Pair.of(NormalDependencyType.BUILDTIME_WEAK, "HEAD")));
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

    public static void downloadIvyModule(IvyCache ivyCache, IvyModuleAndVersion module, Path destinationPath) {
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
        catch(Exception e) {
            throw ExceptionUtils.commute(e);
        }
    }

    private static String getPackageName(IvyModuleAndVersion module) {
        return ("mc." + module.group + "." + module.module).replaceAll("-", ".");
    }
    private static String getRootPackagePath() {
        return "mc/packages";
    }
    private static String getPackagePath(IvyModuleAndVersion module) {
        return getRootPackagePath() + "/" + module.group + "/" + module.module;
    }

    private static ImmutableList<String> createLcQbtMakeContents(String packageName, ImmutableList<String> linkCheckerArgs) {
        ImmutableList.Builder<String> b = ImmutableList.builder();
        b.add("#!/bin/bash");
        b.add("");
        b.add("# This file generated by the third party importer tool");
        b.add("");
        b.add("eval export JAVA_HOME=\\$JAVA_${QBT_ENV_JDK}_HOME");
        b.add("");
        b.add("set -e");
        b.add("");
        b.add("$INPUT_ARTIFACTS_DIR/weak/qbt_fringe.link_checker.release/strong/qbt_fringe.link_checker.release/bin/link_checker --qbtDefaults " + packageName + " " + Joiner.on(' ').join(linkCheckerArgs));
        return b.build();
    }

    private static int compareVersions(String v1, String v2) {
        return new ComparableVersion(v1).compareTo(new ComparableVersion(v2));
    }
}
