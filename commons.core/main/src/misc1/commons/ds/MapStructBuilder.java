package misc1.commons.ds;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;

public class MapStructBuilder<S extends MapStruct<S, B, K, VS, VB>, B extends MapStructBuilder<S, B, K, VS, VB>, K, VS, VB> {
    private final MapStructType<S, B, K, VS, VB> type;
    public final ImmutableSalvagingMap<K, VB> map;

    protected MapStructBuilder(MapStructType<S, B, K, VS, VB> type, ImmutableSalvagingMap<K, VB> map) {
        this.type = type;
        this.map = map;
    }

    public VB get(K key) {
        return map.get(key);
    }

    public Optional<VB> getOptional(K key) {
        if(!map.containsKey(key)) {
            return Optional.empty();
        }
        return Optional.of(map.get(key));
    }

    public B with(K key, VB vb) {
        return type.createBuilder(map.simplePut(key, vb));
    }

    public B withOptional(K key, Optional<VB> mvb) {
        return mvb.map((vb) -> with(key, vb)).orElseGet(() -> without(key));
    }

    public B without(K key) {
        return type.createBuilder(map.simpleRemove(key));
    }

    public B transform(K k, Function<VB, VB> f) {
        return with(k, f.apply(get(k)));
    }

    public B transformOptional(K k, Function<Optional<VB>, Optional<VB>> f) {
        return withOptional(k, f.apply(getOptional(k)));
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
