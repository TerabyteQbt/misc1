package misc1.third_party_tools.ivy;

import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import org.apache.commons.lang3.tuple.Pair;

public final class IvyResult {
    private final ImmutableList<IvyModuleAndVersion> dependencies;
    private final ImmutableList<Pair<Path, Path>> files;

    IvyResult(ImmutableList<IvyModuleAndVersion> dependencies, ImmutableList<Pair<Path, Path>> files) {
        this.dependencies = dependencies;
        this.files = files;
    }

    public ImmutableList<IvyModuleAndVersion> getDependencies() {
        return dependencies;
    }

    public ImmutableList<Pair<Path, Path>> getFiles() {
        return files;
    }
}
