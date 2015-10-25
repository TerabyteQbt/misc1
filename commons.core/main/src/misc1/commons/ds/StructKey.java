package misc1.commons.ds;

import com.google.common.base.Optional;

public class StructKey<S, V> {
    private final String name;
    private final Optional<V> def;

    public StructKey(String name) {
        this.name = name;
        this.def = Optional.absent();
    }

    public StructKey(String name, V v) {
        this.name = name;
        this.def = Optional.of(v);
    }

    public Optional<V> getDefault() {
        return def;
    }

    public V onGet(V v) {
        return v;
    }

    public V onSet(V v) {
        return v;
    }

    @Override
    public String toString() {
        return name;
    }
}
