package misc1.commons.concurrent.treelock;

public class ArrayTreeLock<K> extends TreeLock<K, ArrayTreeLockPath<K>> {
    @Override
    protected TreeLockInterface<? super ArrayTreeLockPath<K>> newChild() {
        return new ArrayTreeLock<K>();
    }
}
