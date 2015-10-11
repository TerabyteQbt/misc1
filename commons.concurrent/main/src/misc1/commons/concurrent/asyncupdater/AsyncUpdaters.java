package misc1.commons.concurrent.asyncupdater;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.Executor;

public final class AsyncUpdaters {
    private AsyncUpdaters() {
        // no
    }

    private enum NoKey {
        INSTANCE;
    }

    private static class MyAsyncUpdate<A extends KeylessAsyncUpdate<A>> implements KeyedAsyncUpdate<NoKey, MyAsyncUpdate<A>> {
        private A delegate;

        public MyAsyncUpdate(A delegate) {
            this.delegate = delegate;
        }

        @Override
        public MyAsyncUpdate<A> merge(MyAsyncUpdate<A> other) {
            delegate = delegate.merge(other.delegate);
            return this;
        }

        @Override
        public void fire() {
            delegate.fire();
        }

        @Override
        public NoKey getKey() {
            return NoKey.INSTANCE;
        }
    }

    private static class MyKeylessAsyncUpdater<A extends KeylessAsyncUpdate<A>> implements KeylessAsyncUpdater<A> {
        private final KeyedAsyncUpdater<NoKey, MyAsyncUpdate<A>> delegate;

        public MyKeylessAsyncUpdater(KeyedAsyncUpdater<NoKey, MyAsyncUpdate<A>> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void enqueue(A event) {
            delegate.enqueue(new MyAsyncUpdate<A>(event));
        }

        @Override
        public ImmutableList<A> getAllInFlightEvents() {
            ImmutableList<MyAsyncUpdate<A>> events = delegate.getAllInFlightEvents();
            List<A> results = Lists.newArrayListWithCapacity(events.size());
            for(MyAsyncUpdate<A> e : delegate.getAllInFlightEvents()) {
                results.add(e.delegate);
            }
            return ImmutableList.copyOf(results);
        }
    }

    public static <K, A extends KeyedAsyncUpdate<K, A>> KeyedAsyncUpdater<K, A> forKeyedSingleThreaded(String name) {
        return new SingleThreadedAsyncUpdater<K, A>(name);
    }

    public static <K, A extends KeyedAsyncUpdate<K, A>> KeyedAsyncUpdater<K, A> forKeyedMultiThreaded(String name) {
        return new MultiThreadedAsyncUpdater<K, A>(name);
    }

    public static <K, A extends KeyedAsyncUpdate<K, A>> KeyedAsyncUpdater<K, A> forKeyedMultiThreaded(Executor executor) {
        return new MultiThreadedAsyncUpdater<K, A>(executor);
    }

    public static <A extends KeylessAsyncUpdate<A>> KeylessAsyncUpdater<A> forKeylessSingleThreaded(String name) {
        KeyedAsyncUpdater<NoKey, MyAsyncUpdate<A>> delegate = forKeyedSingleThreaded(name);
        return new MyKeylessAsyncUpdater<A>(delegate);
    }

    public static <A extends KeylessAsyncUpdate<A>> KeylessAsyncUpdater<A> forKeylessMultiThreaded(String name) {
        KeyedAsyncUpdater<NoKey, MyAsyncUpdate<A>> delegate = forKeyedMultiThreaded(name);
        return new MyKeylessAsyncUpdater<A>(delegate);
    }

    public static <A extends KeylessAsyncUpdate<A>> KeylessAsyncUpdater<A> forKeylessMultiThreaded(Executor executor) {
        KeyedAsyncUpdater<NoKey, MyAsyncUpdate<A>> delegate = forKeyedMultiThreaded(executor);
        return new MyKeylessAsyncUpdater<A>(delegate);
    }
}
