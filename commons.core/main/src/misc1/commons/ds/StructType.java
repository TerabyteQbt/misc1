package misc1.commons.ds;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public abstract class StructType<S> {
    private final ImmutableList<StructKey<S, ?>> keys;

    public StructType(Iterable<StructKey<S, ?>> keys) {
        this.keys = ImmutableList.copyOf(keys);
    }

    public StructBuilder<S> builder() {
        return new StructBuilder<S>(this);
    }

    public S build(ImmutableSalvagingMap<StructKey<S, ?>, Object> map) {
        for(StructKey<S, ?> k : map.keys()) {
            if(!keys.contains(k)) {
                throw new IllegalArgumentException("Nonsense keys: " + k);
            }
        }
        for(StructKey<S, ?> k : keys) {
            Object v = map.get(k);
            if(v == null) {
                Optional<?> mv = k.getDefault();
                if(!mv.isPresent()) {
                    throw new IllegalArgumentException("Key required: " + k);
                }
                map = map.simplePut(k, mv.get());
            }
        }
        return create(map);
    }

    protected abstract S create(ImmutableSalvagingMap<StructKey<S, ?>, Object> map);
}
