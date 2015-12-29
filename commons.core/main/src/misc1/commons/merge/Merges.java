package misc1.commons.merge;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import misc1.commons.Maybe;
import org.apache.commons.lang3.tuple.Triple;

public final class Merges {
    private Merges() {
        // nope
    }

    private static final Merge<Object> TRIVIAL = new Merge<Object>() {
        @Override
        public Triple<Object, Object, Object> merge(Object lhs, Object mhs, Object rhs) {
            if(Objects.equal(lhs, rhs)) {
                return Triple.of(lhs, lhs, lhs);
            }
            if(Objects.equal(mhs, lhs)) {
                return Triple.of(rhs, rhs, rhs);
            }
            if(Objects.equal(mhs, rhs)) {
                return Triple.of(lhs, lhs, lhs);
            }
            return Triple.of(lhs, mhs, rhs);
        }
    };
    public static <V> Merge<V> trivial() {
        return (Merge<V>)TRIVIAL;
    }

    public static <V> Merge<Maybe<V>> maybe(final Merge<V> merge) {
        return new Merge<Maybe<V>>() {
            @Override
            public Triple<Maybe<V>, Maybe<V>, Maybe<V>> merge(Maybe<V> lhs, Maybe<V> mhs, Maybe<V> rhs) {
                if(lhs.isPresent() && mhs.isPresent() && rhs.isPresent()) {
                    Triple<V, V, V> r = merge.merge(lhs.get(null), mhs.get(null), rhs.get(null));
                    return Triple.of(Maybe.of(r.getLeft()), Maybe.of(r.getMiddle()), Maybe.of(r.getRight()));
                }
                return Merges.<Maybe<V>>trivial().merge(lhs, mhs, rhs);
            }
        };
    }

    public static <K, V> Merge<ImmutableMap<K, V>> map(final Merge<Maybe<V>> mergeMaybe) {
        return new Merge<ImmutableMap<K, V>>() {
            @Override
            public Triple<ImmutableMap<K, V>, ImmutableMap<K, V>, ImmutableMap<K, V>> merge(ImmutableMap<K, V> lhs, ImmutableMap<K, V> mhs, ImmutableMap<K, V> rhs) {
                ImmutableSet.Builder<K> keys = ImmutableSet.builder();
                keys.addAll(lhs.keySet());
                keys.addAll(mhs.keySet());
                keys.addAll(rhs.keySet());

                ImmutableMap.Builder<K, V> lhsB = ImmutableMap.builder();
                ImmutableMap.Builder<K, V> mhsB = ImmutableMap.builder();
                ImmutableMap.Builder<K, V> rhsB = ImmutableMap.builder();
                for(K k : keys.build()) {
                    Maybe<V> lhsMaybe = lhs.containsKey(k) ? Maybe.of(lhs.get(k)) : Maybe.<V>not();
                    Maybe<V> mhsMaybe = mhs.containsKey(k) ? Maybe.of(mhs.get(k)) : Maybe.<V>not();
                    Maybe<V> rhsMaybe = rhs.containsKey(k) ? Maybe.of(rhs.get(k)) : Maybe.<V>not();
                    Triple<Maybe<V>, Maybe<V>, Maybe<V>> r = mergeMaybe.merge(lhsMaybe, mhsMaybe, rhsMaybe);
                    Maybe<V> lhsMaybe2 = r.getLeft();
                    Maybe<V> mhsMaybe2 = r.getMiddle();
                    Maybe<V> rhsMaybe2 = r.getRight();
                    if(lhsMaybe2.isPresent()) {
                        lhsB.put(k, lhsMaybe2.get(null));
                    }
                    if(mhsMaybe2.isPresent()) {
                        mhsB.put(k, mhsMaybe2.get(null));
                    }
                    if(rhsMaybe2.isPresent()) {
                        rhsB.put(k, rhsMaybe2.get(null));
                    }
                }
                return Triple.of(lhsB.build(), mhsB.build(), rhsB.build());
            }
        };
    }

    private static final Merge<ImmutableSet<Object>> SET = new Merge<ImmutableSet<Object>>() {
        @Override
        public Triple<ImmutableSet<Object>, ImmutableSet<Object>, ImmutableSet<Object>> merge(ImmutableSet<Object> lhs, ImmutableSet<Object> mhs, ImmutableSet<Object> rhs) {
            ImmutableSet.Builder<Object> all = ImmutableSet.builder();
            all.addAll(lhs);
            all.addAll(mhs);
            all.addAll(rhs);

            ImmutableSet.Builder<Object> b = ImmutableSet.builder();
            for(Object e : all.build()) {
                if((lhs.contains(e) ? 1 : 0) - (mhs.contains(e) ? 1 : 0) + (rhs.contains(e) ? 1 : 0) >= 1) {
                    b.add(e);
                }
            }

            ImmutableSet<Object> r = b.build();
            return Triple.of(r, r, r);
        }
    };
    public static <E> Merge<ImmutableSet<E>> set() {
        return (Merge)SET;
    }
}