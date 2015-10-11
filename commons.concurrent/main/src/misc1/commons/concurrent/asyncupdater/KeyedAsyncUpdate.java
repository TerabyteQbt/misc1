package misc1.commons.concurrent.asyncupdater;

/**
 * An instance of some type of low-QoS notification that has an additional
 * concept of a "key" used to somehow bucket updates.  The behaviour the key
 * affects is defined by the {@link KeyedAsyncUpdater} itself.
 */
public interface KeyedAsyncUpdate<K, A> {
    /**
     * Merge this with other.  Both this and other must belong to the caller
     * and will no longer belong to the caller after this method (unless one is
     * returned).
     */
    A merge(A other);

    /**
     * Actually perform whatever blocking action is required to relay the
     * update.
     */
    void fire();

    /**
     * Returns the key by which to deduplicate/squash updates.  This must be
     * unique per async updater, not just unique among instances of a given
     * class implementing this interface.
     */
    K getKey();
}
