package misc1.commons.ds.union;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import misc1.commons.merge.Merge;
import org.apache.commons.lang3.tuple.Triple;

public class UnionType<U extends Union<U>> {
    public final ImmutableList<UnionKey<U, ?>> keys;
    private final Function<UnionChoice<U, ?>, U> ctor;

    public UnionType(Iterable<UnionKey<U, ?>> keys, Function<UnionChoice<U, ?>, U> ctor) {
        this.keys = ImmutableList.copyOf(keys);
        this.ctor = ctor;
    }

    public <T> U of(UnionKey<U, T> key, T value) {
        return ctor.apply(new UnionChoice<>(key, value));
    }

    public Merge<U> merge() {
        return (lhs, mhs, rhs) -> {
            if(lhs.equals(mhs)) {
                return Triple.of(rhs, rhs, rhs);
            }
            if(rhs.equals(mhs)) {
                return Triple.of(lhs, lhs, lhs);
            }
            if(lhs.equals(rhs)) {
                return Triple.of(lhs, lhs, lhs);
            }
            return handleMerge(mhs.choice.key, lhs, mhs, rhs);
        };
    }

    private <T> Triple<U, U, U> handleMerge(UnionKey<U, T> key, U lhs, U mhs, U rhs) {
        Optional<T> lhsValue = lhs.choice.optional(key);
        if(!lhsValue.isPresent()) {
            return Triple.of(lhs, mhs, rhs);
        }
        Optional<T> mhsValue = mhs.choice.optional(key);
        if(!mhsValue.isPresent()) {
            return Triple.of(lhs, mhs, rhs);
        }
        Optional<T> rhsValue = rhs.choice.optional(key);
        if(!rhsValue.isPresent()) {
            return Triple.of(lhs, mhs, rhs);
        }
        Triple<T, T, T> tr = key.merge().merge(lhsValue.get(), mhsValue.get(), rhsValue.get());
        return Triple.of(ctor.apply(new UnionChoice<>(key, tr.getLeft())), ctor.apply(new UnionChoice<>(key, tr.getMiddle())), ctor.apply(new UnionChoice<>(key, tr.getRight())));
    }
}
