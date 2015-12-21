package misc1.commons.ds;

import com.google.common.collect.ImmutableMap;
import misc1.commons.Maybe;
import misc1.commons.merge.Merge;
import misc1.commons.merge.Merges;
import org.apache.commons.lang3.tuple.Triple;

public abstract class MapStructType<S extends MapStruct<S, B, K, VS, VB>, B, K, VS, VB> {
    protected abstract S create(ImmutableMap<K, VS> map);
    protected abstract B createBuilder(ImmutableSalvagingMap<K, VB> map);
    protected abstract VS toStruct(VB vb);
    protected abstract VB toBuilder(VS vs);

    protected Merge<VS> mergeValue() {
        return Merges.trivial();
    }

    public Merge<S> merge() {
        return new Merge<S>() {
            @Override
            public Triple<S, S, S> merge(S lhs, S mhs, S rhs) {
                Merge<VS> mergeValue = mergeValue();
                Merge<Maybe<VS>> mergeMaybeValue = Merges.maybe(mergeValue);
                Merge<ImmutableMap<K, VS>> mergeMap = Merges.<K, VS>map(mergeMaybeValue);
                Triple<ImmutableMap<K, VS>, ImmutableMap<K, VS>, ImmutableMap<K, VS>> r = mergeMap.merge(lhs.map, mhs.map, rhs.map);
                S lhs2 = create(r.getLeft());
                S mhs2 = create(r.getMiddle());
                S rhs2 = create(r.getRight());
                return Triple.of(lhs2, mhs2, rhs2);
            }
        };
    }

    public B builder() {
        return createBuilder(ImmutableSalvagingMap.<K, VB>of());
    }
}
