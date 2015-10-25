package misc1.commons.ds;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;

public class Struct<S> {
    private final StructType<S> type;
    private final Map<StructKey<S, ?>, Object> map;

    protected Struct(StructType<S> type, Map<StructKey<S, ?>, Object> map) {
        this.type = type;
        this.map = ImmutableMap.copyOf(map);
    }

    public <V> V get(StructKey<S, V> k) {
        return k.onGet((V)map.get(k));
    }

    public <V> S set(StructKey<S, V> k, V v) {
        Map<StructKey<S, ?>, Object> newMap = Maps.newHashMap(map);
        map.put(k, k.onSet(v));
        return type.create(newMap);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + map.toString();
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
        Struct<S> other = (Struct<S>)obj;
        return map.equals(other.map);
    }
}
