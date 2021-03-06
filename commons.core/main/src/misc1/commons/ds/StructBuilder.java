package misc1.commons.ds;

import com.google.common.base.Function;

public abstract class StructBuilder<S extends Struct<S, B>, B extends StructBuilder<S, B>> {
    private final StructType<S, B> type;
    private ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> map;

    protected StructBuilder(StructType<S, B> type, ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> map) {
        this.type = type;
        this.map = map;
    }

    public <VB> B set(StructKey<S, ?, VB> k, VB vb) {
        return type.builderCtor.apply(map.simplePut(k, vb));
    }

    public <VB> VB get(StructKey<S, ?, VB> k) {
        if(map.containsKey(k)) {
            return (VB)map.get(k);
        }
        return k.def.get();
    }

    public <VB> B transform(StructKey<S, ?, VB> k, Function<VB, VB> f) {
        return set(k, f.apply(get(k)));
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!getClass().equals(obj.getClass())) {
            return false;
        }
        StructBuilder<S, B> other = (StructBuilder<S, B>)obj;
        return map.equals(other.map);
    }

    public B apply(Function<B, B> f) {
        return f.apply(type.builderCtor.apply(map));
    }

    public S build() {
        return type.create(map);
    }
}
