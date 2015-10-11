package misc1.commons.concurrent.treelock;

public interface TreeLockPath<K, PR> {
    public K car();
    public PR cdr();
}
