package misc1.commons.ds;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class MapStruct<S extends MapStruct<S, B, K, VS, VB>, B, K, VS, VB> {
    private final MapStructType<S, B, K, VS, VB> type;
    public final ImmutableMap<K, VS> map;

    protected MapStruct(MapStructType<S, B, K, VS, VB> type, ImmutableMap<K, VS> map) {
        this.type = type;
        this.map = map;
    }

    public B builder() {
        ImmutableSalvagingMap<K, VB> b = ImmutableSalvagingMap.of();
        for(Map.Entry<K, VS> e : map.entrySet()) {
            b = b.simplePut(e.getKey(), type.toBuilder(e.getValue()));
        }
        return type.createBuilder(b);
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
        return map.equals(((MapStruct) obj).map);
    }
}
