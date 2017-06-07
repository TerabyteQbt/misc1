package misc1.commons.ds;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import misc1.commons.merge.Merge;

public final class StructKey<S, VS, VB> {
    public final String name;
    final Optional<VB> def;
    final Function<VB, VS> toStruct;
    final Function<VS, VB> toBuilder;
    final Merge<VS> merge;

    public StructKey(String name, Optional<VB> def, Function<VB, VS> toStruct, Function<VS, VB> toBuilder, Merge<VS> merge) {
        this.name = name;
        this.def = def;
        this.toStruct = toStruct;
        this.toBuilder = toBuilder;
        this.merge = merge;
    }

    @Override
    public String toString() {
        return name;
    }
}
