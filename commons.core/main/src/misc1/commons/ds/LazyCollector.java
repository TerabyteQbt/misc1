package misc1.commons.ds;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;

public abstract class LazyCollector<T> {
    private LazyCollector() {
        // hidden
    }

    private static class EmptyLazyCollector<T> extends LazyCollector<T> {
        @Override
        protected void accumulateUncached(Function<T, ?> accumulator, Set<LazyCollector<T>> already) {
        }
    }
    private static final EmptyLazyCollector<Object> EMPTY = new EmptyLazyCollector<Object>();
    @SuppressWarnings("unchecked")
    public static <T> LazyCollector<T> of() {
        return (LazyCollector<T>)EMPTY;
    }

    public static <T> LazyCollector<T> of(Iterable<T> ts) {
        final ImmutableList<T> tsSafe = ImmutableList.copyOf(ts);
        class UnlazyLazyCollector extends LazyCollector<T> {
            @Override
            protected void accumulateUncached(Function<T, ?> accumulator, Set<LazyCollector<T>> already) {
                for(T t : tsSafe) {
                    accumulator.apply(t);
                }
            }
        }
        return new UnlazyLazyCollector();
    }

    public static <T> LazyCollector<T> of(final T t) {
        return of(ImmutableList.of(t));
    }

    private static class OfElementFunction<T> implements Function<T, LazyCollector<T>> {
        @Override
        public LazyCollector<T> apply(T input) {
            return LazyCollector.of(input);
        }
    }
    private static final OfElementFunction<Object> OF_ELEMENT_FUNCTION = new OfElementFunction<Object>();
    @SuppressWarnings("unchecked")
    public static <T> Function<T, LazyCollector<T>> ofElementFunction() {
        return (OfElementFunction<T>)OF_ELEMENT_FUNCTION;
    }

    public LazyCollector<T> union(final LazyCollector<T> other) {
        final LazyCollector<T> self = this;
        return new LazyCollector<T>() {
            @Override
            protected void accumulateUncached(Function<T, ?> accumulator, Set<LazyCollector<T>> already) {
                self.accumulate(accumulator, already);
                other.accumulate(accumulator, already);
            }
        };
    }

    protected abstract void accumulateUncached(Function<T, ?> accumulator, Set<LazyCollector<T>> already);

    protected void accumulate(Function<T, ?> accumulator, Set<LazyCollector<T>> already) {
        if(!already.add(this)) {
            return;
        }
        accumulateUncached(accumulator, already);
    }

    public ImmutableSet<T> forceSet() {
        final ImmutableSet.Builder<T> b = ImmutableSet.builder();
        accumulate(new Function<T, Void>() {
            @Override
            public Void apply(T t) {
                b.add(t);
                return null;
            }
        });
        return b.build();
    }

    public ImmutableList<T> forceList() {
        final ImmutableList.Builder<T> b = ImmutableList.builder();
        accumulate(new Function<T, Void>() {
            @Override
            public Void apply(T t) {
                b.add(t);
                return null;
            }
        });
        return b.build();
    }

    public void accumulate(Function<T, ?> accumulator) {
        Set<LazyCollector<T>> already = Sets.newHashSet();
        accumulate(accumulator, already);
    }
}
