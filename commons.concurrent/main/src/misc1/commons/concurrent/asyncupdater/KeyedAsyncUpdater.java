package misc1.commons.concurrent.asyncupdater;

import com.google.common.collect.ImmutableList;

public interface KeyedAsyncUpdater<K, A extends KeyedAsyncUpdate<K, A>> {
    void enqueue(A event);
    ImmutableList<A> getAllInFlightEvents();
}
