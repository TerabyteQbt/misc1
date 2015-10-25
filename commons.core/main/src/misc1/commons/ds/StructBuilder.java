package misc1.commons.ds;

import com.google.common.collect.Maps;
import java.util.Map;

public class StructBuilder<S> {
    private final StructType<S> type;
    private final Map<StructKey<S, ?>, Object> map = Maps.newHashMap();

    public StructBuilder(StructType<S> type) {
        this.type = type;
    }

    public <V> StructBuilder<S> set(StructKey<S, V> k, V v) {
        map.put(k, k.onSet(v));
        return this;
    }

    public S build() {
        return type.build(map);
    }
}
