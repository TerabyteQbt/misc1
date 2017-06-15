package misc1.commons.ds.union;

import com.google.common.base.Function;

public interface UnionVisit<U extends Union<U>, R> {
    <T> UnionVisit<U, R> on(UnionKey<U, T> key, Function<T, R> fn);
    R complete(Function<U, R> def);
    default R complete(R def) {
        return complete((u) -> def);
    }
    default R complete() {
        return complete((u) -> {
            throw new IllegalStateException("Unexpected union key: " + u.choice.key);
        });
    }
}
