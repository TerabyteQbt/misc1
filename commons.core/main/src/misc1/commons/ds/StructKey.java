package misc1.commons.ds;

import com.google.common.base.Optional;
import misc1.commons.merge.Merge;
import misc1.commons.merge.Merges;

public abstract class StructKey<S, VS, VB> {
    public final String name;
    private final Optional<VB> def;

    public StructKey(String name) {
        this.name = name;
        this.def = Optional.absent();
    }

    public StructKey(String name, VB vb) {
        this.name = name;
        this.def = Optional.of(vb);
    }

    public StructKey(String name, Optional<VB> def) {
        this.name = name;
        this.def = def;
    }

    public Optional<VB> getDefault() {
        return def;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract VS toStruct(VB vb);
    public abstract VB toBuilder(VS vs);

    public Merge<VS> merge() {
        return Merges.trivial();
    }
}
