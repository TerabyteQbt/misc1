package misc1.commons.ds;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class MapStructBuilder<S extends MapStruct<S, B, K, VS, VB>, B, K, VS, VB> {
    private final MapStructType<S, B, K, VS, VB> type;
    public final ImmutableSalvagingMap<K, VB> map;

    protected MapStructBuilder(MapStructType<S, B, K, VS, VB> type, ImmutableSalvagingMap<K, VB> map) {
        this.type = type;
        this.map = map;
    }

    public VB get(K key) {
        return map.get(key);
    }

    public B with(K key, VB vb) {
        return type.createBuilder(map.simplePut(key, vb));
    }

    public B without(K key) {
        return type.createBuilder(map.simpleRemove(key));
    }

    public S build() {
        ImmutableMap.Builder<K, VS> b = ImmutableMap.builder();
        for(Map.Entry<K, VB> e : map.entries()) {
            b.put(e.getKey(), type.toStruct(e.getValue()));
        }
        return type.create(b.build());
    }

    @Override
    public final int hashCode() {
        return map.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!getClass().equals(obj.getClass())) {
            return false;
        }
        return map.equals(((MapStructBuilder) obj).map);
    }
}
