package misc1.commons.ds;

public interface WrapperType<W, D> {
    D unwrap(W w);
    W wrap(D d);
}
