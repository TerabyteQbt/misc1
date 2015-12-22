package misc1.commons.ds;

public class SimpleStructKey<S, V> extends StructKey<S, V, V> {
    public SimpleStructKey(String name) {
        super(name);
    }

    public SimpleStructKey(String name, V v) {
        super(name, v);
    }

    @Override
    public V toStruct(V v) {
        return v;
    }

    @Override
    public V toBuilder(V v) {
        return v;
    }
}
