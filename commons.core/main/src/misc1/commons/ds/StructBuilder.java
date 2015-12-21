package misc1.commons.ds;

public abstract class StructBuilder<S extends Struct<S, B>, B extends StructBuilder<S, B>> {
    private final StructType<S, B> type;
    private ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> map;

    protected StructBuilder(StructType<S, B> type, ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> map) {
        this.type = type;
        this.map = map;
    }

    public <VB> B set(StructKey<S, ?, VB> k, VB vb) {
        return type.createBuilder(map.simplePut(k, vb));
    }

    public <VB> VB get(StructKey<S, ?, VB> k) {
        if(map.containsKey(k)) {
            return (VB)map.get(k);
        }
        return k.getDefault().get();
    }

    public S build() {
        return type.create(map);
    }
}
