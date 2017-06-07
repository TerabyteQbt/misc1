package misc1.commons.ds;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import misc1.commons.merge.Merge;
import misc1.commons.merge.Merges;

public class StructTypeBuilder<S extends Struct<S, B>, B extends StructBuilder<S, B>> {
    private final ImmutableList.Builder<StructKey<S, ?, ?>> keys = ImmutableList.builder();
    private final Function<ImmutableMap<StructKey<S, ?, ?>, Object>, S> structCtor;
    private final Function<ImmutableSalvagingMap<StructKey<S, ?, ?>, Object>, B> builderCtor;

    public StructTypeBuilder(Function<ImmutableMap<StructKey<S, ?, ?>, Object>, S> structCtor, Function<ImmutableSalvagingMap<StructKey<S, ?, ?>, Object>, B> builderCtor) {
        this.structCtor = structCtor;
        this.builderCtor = builderCtor;
    }

    public class StructKeyBuilder<VS, VB> {
        private final String name;
        private final Function<VB, VS> toStruct;
        private final Function<VS, VB> toBuilder;

        private Optional<VB> def = Optional.absent();
        private Merge<VS> merge = Merges.trivial();

        private StructKeyBuilder(String name, Function<VB, VS> toStruct, Function<VS, VB> toBuilder) {
            this.name = name;
            this.toStruct = toStruct;
            this.toBuilder = toBuilder;
        }

        public StructKeyBuilder<VS, VB> merge(Merge<VS> merge) {
            this.merge = merge;
            return this;
        }

        public StructKeyBuilder<VS, VB> def(VB vb) {
            this.def = Optional.of(vb);
            return this;
        }

        public StructKey<S, VS, VB> add() {
            StructKey<S, VS, VB> key = new StructKey<>(name, def, toStruct, toBuilder, merge);
            keys.add(key);
            return key;
        }
    }

    public <V> StructKeyBuilder<V, V> key(String name) {
        return new StructKeyBuilder<>(name, v -> v, v -> v);
    }

    public <VS, VB> StructKeyBuilder<VS, VB> key(String name, Function<VB, VS> toStruct, Function<VS, VB> toBuilder) {
        return new StructKeyBuilder<>(name, toStruct, toBuilder);
    }

    public <VS extends MapStruct<VS, VB, K, VVS, VVB>, VB extends MapStructBuilder<VS, VB, K, VVS, VVB>, K, VVS, VVB> StructKeyBuilder<VS, VB> key(String name, MapStructType<VS, VB, K, VVS, VVB> type) {
        return new StructKeyBuilder<>(name, VB::build, VS::builder).def(type.builder());
    }

    public <VS extends Struct<VS, VB>, VB extends StructBuilder<VS, VB>> StructKeyBuilder<VS, VB> key(String name, StructType<VS, VB> type) {
        return new StructKeyBuilder<>(name, VB::build, VS::builder).def(type.builder());
    }

    public StructType<S, B> build() {
        return new StructType<S, B>(keys.build(), structCtor, builderCtor);
    }
}
