package misc1.commons.ds;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class MapStructBuilder<S, B, K, VS, VB> {
    private final MapStructType<S, B, K, VS, VB> type;
    private final ImmutableSalvagingMap<K, VB> map;

    protected MapStructBuilder(MapStructType<S, B, K, VS, VB> type, ImmutableSalvagingMap<K, VB> map) {
        this.type = type;
        this.map = map;
    }

    public B with(K key, VB vb) {
        return type.createBuilder(map.simplePut(key, vb));
    }

    public S build() {
        ImmutableMap.Builder<K, VS> b = ImmutableMap.builder();
        for(Map.Entry<K, VB> e : map.entries()) {
            b.put(e.getKey(), type.toStruct(e.getValue()));
        }
        return type.create(b.build());
    }
}
