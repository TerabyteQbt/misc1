package misc1.third_party_tools.ivy;

import java.nio.file.Path;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

public final class IvyResult {
    private final ImmutableList<IvyModuleAndVersion> dependencies;
    private final ImmutableList<Pair<Path, Path>> files;
    private final ImmutableList<Pair<String, String>> licenses; // name, url

    IvyResult(ImmutableList<IvyModuleAndVersion> dependencies, ImmutableList<Pair<Path, Path>> files, ImmutableList<Pair<String, String>> licenses) {
        this.dependencies = dependencies;
        this.files = files;
        this.licenses = licenses;
    }

    public ImmutableList<IvyModuleAndVersion> getDependencies() {
        return dependencies;
    }

    public ImmutableList<Pair<Path, Path>> getFiles() {
        return files;
    }

    public ImmutableList<Pair<String, String>> getLicenses() {
        return licenses;
    }
}
