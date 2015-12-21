package misc1.commons.ds;

public abstract class SimpleMapStructType<S, B, K, V> extends MapStructType<S, B, K, V, V> {
    @Override
    protected V toStruct(V vb) {
        return vb;
    }

    @Override
    protected V toBuilder(V vs) {
        return vs;
    }
}
