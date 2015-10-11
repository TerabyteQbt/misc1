package misc1.commons.concurrent.asyncupdater;

/**
 * An instance of some type of low-QoS notification.
 */
public interface KeylessAsyncUpdate<A> {
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
}
