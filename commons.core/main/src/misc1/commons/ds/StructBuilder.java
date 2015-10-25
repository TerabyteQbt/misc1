package misc1.commons.ds;

public class StructBuilder<S> {
    private final StructType<S> type;
    private ImmutableSalvagingMap<StructKey<S, ?>, Object> map = ImmutableSalvagingMap.of();

    public StructBuilder(StructType<S> type) {
        this.type = type;
    }

    public <V> StructBuilder<S> set(StructKey<S, V> k, V v) {
        map = map.simplePut(k, k.onSet(v));
        return this;
    }

    public S build() {
        return type.build(map);
    }
}
