package misc1.commons.concurrent.treelock;

public class SimpleTreeLockPath<K, PR> implements TreeLockPath<K, PR> {
    private final K car;
    private final PR cdr;

    public SimpleTreeLockPath(K car, PR cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    @Override
    public K car() {
        return car;
    }

    @Override
    public PR cdr() {
        return cdr;
    }

    public static <K, PR> SimpleTreeLockPath<K, PR> of(K car, PR cdr) {
        return new SimpleTreeLockPath<K, PR>(car, cdr);
    }
}
