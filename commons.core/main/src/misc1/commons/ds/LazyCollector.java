package misc1.commons.ds;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

public final class LazyCollector<T> {
    public final ImmutableList<T> ts;
    public final ImmutableList<LazyCollector<T>> delegates;

    private LazyCollector(ImmutableList<T> ts, ImmutableList<LazyCollector<T>> delegates) {
        this.ts = ts != null ? ts : ImmutableList.<T>of();
        this.delegates = delegates != null ? delegates : ImmutableList.<LazyCollector<T>>of();
    }

    private static final LazyCollector<Object> EMPTY = new LazyCollector<Object>(null, null);
    @SuppressWarnings("unchecked")
    public static <T> LazyCollector<T> of() {
        return (LazyCollector<T>)EMPTY;
    }

    public static <T> LazyCollector<T> of(Iterable<T> ts) {
        return new LazyCollector<T>(ImmutableList.copyOf(ts), null);
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
        return new LazyCollector(null, ImmutableList.of(this, other));
    }

    public static <T> LazyCollector<T> unionIterable(final Iterable<LazyCollector<T>> others) {
        return new LazyCollector(null, ImmutableList.copyOf(others));
    }

    public ImmutableSet<T> forceSet() {
        final ImmutableSet.Builder<T> b = ImmutableSet.builder();
        accumulate((t) -> {
            b.add(t);
            return null;
        });
        return b.build();
    }

    public ImmutableList<T> forceList() {
        final ImmutableList.Builder<T> b = ImmutableList.builder();
        accumulate((t) -> {
            b.add(t);
            return null;
        });
        return b.build();
    }

    public void accumulate(final Function<T, ?> accumulator) {
        Set<LazyCollector<T>> already = Sets.newHashSet();
        Deque<LazyCollector<T>> stack = Lists.newLinkedList();
        stack.addLast(this);
        while(!stack.isEmpty()) {
            LazyCollector<T> next = stack.removeLast();
            if(!already.add(next)) {
                continue;
            }
            final List<LazyCollector<T>> children = Lists.newArrayList();
            next.accept(new Visitor<T>() {
                @Override
                public void visitElement(T t) {
                    accumulator.apply(t);
                }

                @Override
                public void visitChild(LazyCollector<T> lc) {
                    children.add(lc);
                }
            });
            Collections.reverse(children);
            for(LazyCollector<T> child : children) {
                stack.addLast(child);
            }
        }
    }

    public interface Visitor<T> {
        public void visitElement(T t);
        public void visitChild(LazyCollector<T> lc);
    }

    public void accept(Visitor<T> visitor) {
        for(T t : ts) {
            visitor.visitElement(t);
        }
        for(LazyCollector<T> delegate : delegates) {
            visitor.visitChild(delegate);
        }
    }
}
