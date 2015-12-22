package misc1.commons.ds;

import com.google.common.collect.ImmutableMap;

public class Struct<S, B extends StructBuilder<S, B>> {
    private final StructType<S, B> type;
    private final ImmutableMap<StructKey<S, ?, ?>, Object> map;

    protected Struct(StructType<S, B> type, ImmutableMap<StructKey<S, ?, ?>, Object> map) {
        this.type = type;
        this.map = map;
    }

    public <VS> VS get(StructKey<S, VS, ?> k) {
        return (VS)map.get(k);
    }

    public <VB> S set(StructKey<S, ?, VB> k, VB vb) {
        return builder().set(k, vb).build();
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
        Struct<S, B> other = (Struct<S, B>)obj;
        return map.equals(other.map);
    }

    public B builder() {
        B b = type.builder();
        for(StructKey<S, ?, ?> k : map.keySet()) {
            b = copyKey(b, k);
        }
        return b;
    }

    private <VS, VB> B copyKey(B b, StructKey<S, VS, VB> k) {
        VS vs = get(k);
        return b.set(k, k.toBuilder(vs));
    }
}
