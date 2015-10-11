package misc1.commons.concurrent.treelock;

public interface TreeLockInterface<P> {
    public void lock(P path);
    public void unlock(P path);
}
