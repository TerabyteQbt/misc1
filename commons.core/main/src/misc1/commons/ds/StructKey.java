package misc1.commons.ds;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import misc1.commons.json.JsonSerializer;
import misc1.commons.merge.Merge;

public final class StructKey<S, VS, VB> {
    public final String name;
    final Optional<VB> def;
    final Function<VB, VS> toStruct;
    final Function<VS, VB> toBuilder;
    final Merge<VS> merge;
    final Optional<JsonSerializer<VB>> serializer;

    StructKey(String name, Optional<VB> def, Function<VB, VS> toStruct, Function<VS, VB> toBuilder, Merge<VS> merge, Optional<JsonSerializer<VB>> serializer) {
        this.name = name;
        this.def = def;
        this.toStruct = toStruct;
        this.toBuilder = toBuilder;
        this.merge = merge;
        this.serializer = serializer;
    }

    @Override
    public String toString() {
        return name;
    }
}
