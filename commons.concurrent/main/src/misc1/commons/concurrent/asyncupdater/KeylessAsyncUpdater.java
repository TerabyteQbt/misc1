package misc1.commons.concurrent.asyncupdater;

import com.google.common.collect.ImmutableList;

public interface KeylessAsyncUpdater<A> {
    void enqueue(A event);
    ImmutableList<A> getAllInFlightEvents();
}
