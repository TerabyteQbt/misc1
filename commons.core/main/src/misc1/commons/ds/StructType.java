package misc1.commons.ds;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import misc1.commons.ds.ImmutableSalvagingMap;

public abstract class StructType<S, B> {
    private final ImmutableList<StructKey<S, ?, ?>> keys;

    public StructType(Iterable<StructKey<S, ?, ?>> keys) {
        this.keys = ImmutableList.copyOf(keys);
    }

    public B builder() {
        return createBuilder(ImmutableSalvagingMap.<StructKey<S, ?, ?>, Object>of());
    }

    public final S create(ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> map) {
        for(StructKey<S, ?, ?> k : map.keys()) {
            if(!keys.contains(k)) {
                throw new IllegalArgumentException("Nonsense keys: " + k);
            }
        }
        ImmutableMap.Builder<StructKey<S, ?, ?>, Object> b = ImmutableMap.builder();
        for(StructKey<S, ?, ?> k : keys) {
            copyKey(b, map, k);
        }
        return createUnchecked(b.build());
    }

    private static <S, VS, VB> void copyKey(ImmutableMap.Builder<StructKey<S, ?, ?>, Object> b, ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> map, StructKey<S, VS, VB> k) {
        VB vb = (VB)map.get(k);
        if(vb == null) {
            Optional<VB> mv = k.getDefault();
            if(!mv.isPresent()) {
                throw new IllegalArgumentException("Key required: " + k);
            }
            vb = mv.get();
        }
        VS vs = k.toStruct(vb);
        b.put(k, vs);
    }

    protected abstract S createUnchecked(ImmutableMap<StructKey<S, ?, ?>, Object> map);
    protected abstract B createBuilder(ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> map);
}
