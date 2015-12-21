package misc1.third_party_tools.mains;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;
import misc1.third_party_tools.ivy.IvyCache;
import misc1.third_party_tools.ivy.IvyModuleAndVersion;
import misc1.third_party_tools.ivy.IvyResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import qbt.NormalDependencyType;
import qbt.QbtCommand;
import qbt.QbtMain;
import qbt.QbtTempDir;
import qbt.QbtUtils;
import qbt.manifest.QbtManifest;
import qbt.tip.PackageTip;
import qbt.tip.RepoTip;
import qbt.utils.TarballUtils;

// TODO: remove dep on mockito?
public final class ImportThirdPartyTest {

    @Rule
    public final TemporaryFolder temporaryFolderRule = new TemporaryFolder();

    // TODO: remove network dependency
    @Ignore
    @Test
    public void testDownloadIvyModule() throws Exception {
        try(QbtTempDir tempDir = new QbtTempDir()) {
            IvyResult ivyResult = new IvyCache(tempDir.path).queryIvy(new IvyModuleAndVersion("junit:junit:4.12"));
            Assert.assertEquals(1, ivyResult.getFiles().size());
        }
    }

    @Ignore
    @Test
    public void testGetIvyDependencies() throws IOException, ParseException {
        try(QbtTempDir tempDir = new QbtTempDir()) {
            ImmutableList<IvyModuleAndVersion> dependencies = new IvyCache(tempDir.path).queryIvy(new IvyModuleAndVersion("junit:junit:4.12")).getDependencies();
            Assert.assertEquals(1, dependencies.size());
            Assert.assertEquals("org.hamcrest:hamcrest-core:1.3", dependencies.get(0).toString());
        }
    }

    // TODO: stolen from qbt.app.test's IntegrationTests.java
    private Path unpackWorkspace(String name) throws IOException {
        final URL tarballUrl = ImportThirdParty.class.getClassLoader().getResource("META-INF/third_party_tools/test-repos/IntegrationTests-"+name+".tar.gz");
        Path tarballFile = temporaryFolderRule.newFile().toPath();
        try(InputStream is = Resources.asByteSource(tarballUrl).openStream(); OutputStream os = QbtUtils.openWrite(tarballFile)) {
            ByteStreams.copy(is, os);
        }
        Path workspace = temporaryFolderRule.newFolder().toPath();
        TarballUtils.explodeTarball(workspace, tarballFile);
        QbtUtils.delete(tarballFile);
        return workspace;
    }

    // TODO: stolen from qbt.app.test's IntegrationTests.java
    private int runInstance(Path workspace, QbtCommand<?> instance, String...args) throws Exception {
        ImmutableList.Builder<String> argsBuilder = ImmutableList.builder();
        argsBuilder.add("--config");
        argsBuilder.add(workspace.resolve("qbt-config").toAbsolutePath().toString());
        argsBuilder.add("--manifest");
        argsBuilder.add(workspace.resolve("qbt-manifest").toAbsolutePath().toString());
        for(String arg : args) {
            argsBuilder.add(arg);
        }
        return QbtMain.runInstance(instance, argsBuilder.build(), false);
    }
    @Ignore
    @Test
    public void testSimpleNoDependencies() throws Exception {
        Path workspace = unpackWorkspace("testCreateThirdParty");
        ImportThirdParty itp = new ImportThirdParty();
        Assert.assertEquals(0, runInstance(workspace, itp, "--module", "org.hamcrest:hamcrest-core:1.3", "--repo", "tp"));
        Path jarPath = workspace.resolve("local/HEAD/tp/mc/org.hamcrest/hamcrest.core/src/jars/hamcrest-core.jar");
        Assert.assertTrue(jarPath.toFile().exists());
        Path qbtMakePath = workspace.resolve("local/HEAD/tp/mc/org.hamcrest/hamcrest.core/qbt-make");
        Assert.assertTrue(qbtMakePath.toFile().exists());
        Assert.assertTrue(qbtMakePath.toFile().canExecute());

    }
    @Ignore
    @Test
    public void testWithDeps() throws Exception {
        Path workspace = unpackWorkspace("testCreateThirdParty");
        ImportThirdParty itp = new ImportThirdParty();
        Assert.assertEquals(0, runInstance(workspace, itp, "--module", "junit:junit:4.12", "--repo", "tp"));
        Path junitJarPath = workspace.resolve("local/HEAD/tp/mc/junit/junit/src/jars/junit.jar");
        Assert.assertTrue(junitJarPath.toFile().exists());
        Path hamcrestJarPath = workspace.resolve("local/HEAD/tp/mc/org.hamcrest/hamcrest.core/src/jars/hamcrest-core.jar");
        Assert.assertTrue(hamcrestJarPath.toFile().exists());
        QbtManifest m = QbtManifest.parse(workspace.resolve("qbt-manifest"));
        Assert.assertTrue(m.packageToRepo.containsKey(PackageTip.TYPE.parseRequire("mc.junit.junit")));
        Assert.assertTrue(m.packageToRepo.containsKey(PackageTip.TYPE.parseRequire("mc.org.hamcrest.hamcrest.core")));

    }

    @Ignore
    @Test
    public void testUpgrade() throws Exception {
        Path workspace = unpackWorkspace("testUpgradeThirdParty");
        // So in this workspace, we have junit 3.7 which has no deps
        Path junitMDPath = workspace.resolve("local/HEAD/tp/mc/junit/junit/maven-module.info");
        Path hamcrestMDPath = workspace.resolve("local/HEAD/tp/mc/org.hamcrest/hamcrest.core/maven-module.info");
        Assert.assertTrue(junitMDPath.toFile().exists());
        Assert.assertEquals("Maven Module Version junit:junit:3.7", QbtUtils.readLines(workspace.resolve("local/HEAD/tp/mc/junit/junit/maven-module.info")).get(0));
        Assert.assertFalse(hamcrestMDPath.toFile().exists());

        ImportThirdParty itp = new ImportThirdParty();
        Assert.assertEquals(0, runInstance(workspace, itp, "--module", "junit:junit:4.12", "--repo", "tp"));

        Assert.assertEquals("Maven Module Version junit:junit:4.12", QbtUtils.readLines(junitMDPath).get(0));
        Assert.assertEquals("Maven Module Version org.hamcrest:hamcrest-core:1.3", QbtUtils.readLines(hamcrestMDPath).get(0));
        QbtManifest m = QbtManifest.parse(workspace.resolve("qbt-manifest"));
        Assert.assertTrue(m.packageToRepo.containsKey(PackageTip.TYPE.parseRequire("mc.junit.junit")));
        Assert.assertTrue(m.packageToRepo.containsKey(PackageTip.TYPE.parseRequire("mc.org.hamcrest.hamcrest.core")));
        Assert.assertEquals(Pair.of(NormalDependencyType.STRONG, "HEAD"), m.repos.get(RepoTip.TYPE.parseRequire("tp")).packages.get("mc.junit.junit").normalDeps.get("mc.org.hamcrest.hamcrest.core"));
    }

    @Ignore
    @Test
    public void testNoUpgrade() throws Exception {
        Path workspace = unpackWorkspace("testNoUpgradeThirdParty");
        // So in this workspace, we have junit 4.12 already but no hamcrest (which junit depends upon).
        // the importer should see junit is updated and not try to update hamcrest.
        ImportThirdParty itp = new ImportThirdParty();
        Assert.assertEquals(0, runInstance(workspace, itp, "--module", "junit:junit:4.12", "--repo", "tp"));

        QbtManifest m = QbtManifest.parse(workspace.resolve("qbt-manifest"));
        Assert.assertTrue(m.packageToRepo.containsKey(PackageTip.TYPE.parseRequire("mc.junit.junit")));
        Assert.assertFalse(m.packageToRepo.containsKey(PackageTip.TYPE.parseRequire("mc.org.hamcrest.hamcrest.core")));
    }

    @Ignore
    @Test
    public void testWithEmptyArtifacts() throws Exception {
        Path workspace = unpackWorkspace("testCreateThirdParty");
        ImportThirdParty itp = new ImportThirdParty();
        Assert.assertEquals(0, runInstance(workspace, itp, "--module", "com.jcraft:jsch.agentproxy:0.0.6", "--repo", "tp"));
        QbtManifest m = QbtManifest.parse(workspace.resolve("qbt-manifest"));
        Assert.assertTrue(m.packageToRepo.containsKey(PackageTip.TYPE.parseRequire("mc.com.jcraft.jsch.agentproxy")));
    }

    @Ignore
    @Test
    public void testWithNoOverride() throws Exception {
        Path workspace = unpackWorkspace("testCreateThirdParty");
        ImportThirdParty itp = new ImportThirdParty();
        try {
            Assert.assertEquals(0, runInstance(workspace, itp, "--module", "com.jcraft:jsch.agentproxy:0.0.6", "--repo", "doesntexist"));
            Assert.fail("Should throw when the given repo doesn't exist as a local override");
        }
        catch(IllegalArgumentException e) {
            // pass
        }
    }
}
