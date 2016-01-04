package misc1.third_party_tools.ivy;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import misc1.commons.ExceptionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qbt.QbtHashUtils;
import qbt.QbtTempDir;
import qbt.QbtUtils;

public class IvyCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(IvyCache.class);

    private final Path root;

    public IvyCache(Path root) {
        this.root = root;
    }

    public IvyResult queryIvy(final IvyModuleAndVersion mv) {
        final Path moduleDir = root.resolve(QbtHashUtils.of(mv.toString()).toString());

        QbtUtils.semiAtomicDirCache(moduleDir, "", (moduleTempDir) -> {
            try {
                ImmutableList<IvyModuleAndVersion> deps = ivyResolveAndRetrieve(mv, null, ImmutableList.of("master"));
                QbtUtils.writeLines(moduleTempDir.resolve("dependencies"), Iterables.transform(deps, Functions.toStringFunction()));
                // fetch sources into a separate directory (TODO: change this? have to update eclipsegen)
                ivyResolveAndRetrieve(mv, moduleTempDir.resolve("files").resolve("jars"), ImmutableList.of("master", "compile", "runtime", "provided"));
                ivyResolveAndRetrieve(mv, moduleTempDir.resolve("files").resolve("sources"), ImmutableList.of("sources"));
                return ObjectUtils.NULL;
            }
            catch(Exception e) {
                throw ExceptionUtils.commute(e);
            }
        });

        ImmutableList<IvyModuleAndVersion> deps = ImmutableList.copyOf(Iterables.transform(QbtUtils.readLines(moduleDir.resolve("dependencies")), IvyModuleAndVersion.PARSE));

        final ImmutableList.Builder<Pair<Path, Path>> filesBuilder = ImmutableList.builder();

        if(Files.isDirectory(moduleDir.resolve("files"))) {
            try {
                Files.walkFileTree(moduleDir.resolve("files"), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path absolute = moduleDir.resolve("files").resolve(file);
                        Path relative = moduleDir.resolve("files").relativize(absolute);
                        filesBuilder.add(Pair.of(relative, absolute));
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            catch(Exception e) {
                throw ExceptionUtils.commute(e);
            }
        }

        return new IvyResult(deps, filesBuilder.build());
    }

    /*
     * JESUS FUCKING CHRIST this sucks but thankfully this post has gotten it's cult all over my cargo:
     * https://stackoverflow.com/questions/15598612/simplest-ivy-code-to-programmatically-retrieve-dependency-from-maven-central/22455451#22455451
     *
     */
    private static ImmutableList<IvyModuleAndVersion> ivyResolveAndRetrieve(IvyModuleAndVersion module, Path destinationPath, ImmutableList<String> configs) throws ParseException, IOException {
        LOGGER.debug("Processing configs: " + StringUtils.join(configs, ", "));
        try(QbtTempDir cacheDir = new QbtTempDir()) {
            IvySettings is = new IvySettings();
            is.setDefaultCache(cacheDir.path.toFile());

            // TODO: add sun repo to get javax crap?
            IBiblioResolver br = new IBiblioResolver();
            br.setM2compatible(true);
            br.setUsepoms(true);
            br.setName("mavenCentral");
            is.addResolver(br);
            is.setDefaultResolver(br.getName());

            Ivy ivy = Ivy.newInstance(is);

            ResolveOptions ro = new ResolveOptions();
            ro.setTransitive(true);
            ro.setDownload(true);

            // we have to create a fake module which includes the module we actually want, so we have to set transitive is true here, and give a fake name.  yes, this is fuckign retarded.
            DefaultModuleDescriptor dmd = DefaultModuleDescriptor.newDefaultInstance(ModuleRevisionId.newInstance(module.group, module.module + "-bucket", module.version));

            // Now we add the "real" deps we care about
            ModuleRevisionId ri = ModuleRevisionId.newInstance(module.group, module.module, module.version);
            // Here we are going to say "not transitive", because we just want to grab the direct dep and then decide later whether or not to keep going.
            DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(dmd,  ri, false, false, false);

            // See: http://lightguard-jp.blogspot.de/2009/04/ivy-configurations-when-pulling-from.html
            // "master" would get us just the code jar, but we want source too... but we don't want javadoc or test probably...
            // Do we need "optional"? "test"?  Probably not test.
            for(String config : configs) {
                dd.addDependencyConfiguration("default", config);
            }
            dmd.addDependency(dd);

            // now resolve
            ResolveReport rr = ivy.resolve(dmd, ro);
            if(rr.hasError()) {
                throw new RuntimeException(rr.getAllProblemMessages().toString());
            }

            ModuleDescriptor m = rr.getModuleDescriptor();
            IvyNode ourModule = (IvyNode) rr.getDependencies().get(0);
            ModuleDescriptor ds = ourModule.getDescriptor();
            ImmutableList.Builder<IvyModuleAndVersion> dependencyModuleStrings = ImmutableList.builder();
            for(DependencyDescriptor depD : ds.getDependencies()) {
                ModuleRevisionId mrid = depD.getDependencyRevisionId();
                dependencyModuleStrings.add(new IvyModuleAndVersion(mrid.getOrganisation(), mrid.getName(), mrid.getRevision()));
            }
            // now retrieve
            // ignore classifier because (e.g. sources) should have the same name
            // was: "/[artifact](-[classifier]).[ext]"
            if(destinationPath != null) {
                String pattern = destinationPath.toAbsolutePath() + "/[artifact]-[revision].[ext]";
                ivy.retrieve(m.getModuleRevisionId(), pattern, new RetrieveOptions().setConfs(new String[]{"default"}));
            }
            return dependencyModuleStrings.build();
        }
    }
}
