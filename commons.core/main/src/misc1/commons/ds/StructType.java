package misc1.commons.ds;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;

public abstract class StructType<S> {
    private final ImmutableList<StructKey<S, ?>> keys;

    public StructType(Iterable<StructKey<S, ?>> keys) {
        this.keys = ImmutableList.copyOf(keys);
    }

    public StructBuilder<S> builder() {
        return new StructBuilder<S>(this);
    }

    public S build(Map<StructKey<S, ?>, Object> map) {
        Set<StructKey<S, ?>> unused = Sets.newHashSet(map.keySet());
        ImmutableMap.Builder<StructKey<S, ?>, Object> b = ImmutableMap.builder();
        for(StructKey<S, ?> k : keys) {
            Object v = unused.remove(k);
            if(v == null) {
                Optional<?> mv = k.getDefault();
                if(!mv.isPresent()) {
                    throw new IllegalArgumentException("Key required: " + k);
                }
                v = mv.get();
            }
            b.put(k, v);
        }
        if(!unused.isEmpty()) {
            throw new IllegalArgumentException("Nonsense keys: " + unused);
        }
        return create(b.build());
    }

    public abstract S create(Map<StructKey<S, ?>, Object> map);
}
