package misc1.commons.ds;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class Struct<S> {
    private final StructType<S> type;
    private final ImmutableSalvagingMap<StructKey<S, ?>, Object> map;

    protected Struct(StructType<S> type, ImmutableSalvagingMap<StructKey<S, ?>, Object> map) {
        this.type = type;
        this.map = map;
    }

    public <V> V get(StructKey<S, V> k) {
        return k.onGet((V)map.get(k));
    }

    public <V> S set(StructKey<S, V> k, V v) {
        return type.create(map.simplePut(k, k.onSet(v)));
    }

    private ImmutableMap<StructKey<S, ?>, Object> toMap() {
        ImmutableMap.Builder<StructKey<S, ?>, Object> b = ImmutableMap.builder();
        for(Map.Entry<StructKey<S, ?>, Object> e : map.entries()) {
            b.put(e);
        }
        return b.build();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + toMap().toString();
    }

    @Override
    public int hashCode() {
        return toMap().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!getClass().equals(obj.getClass())) {
            return false;
        }
        Struct<S> other = (Struct<S>)obj;
        return toMap().equals(other.toMap());
    }
}
