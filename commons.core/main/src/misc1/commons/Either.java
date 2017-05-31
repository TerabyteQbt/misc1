package misc1.commons;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;

public abstract class Either<L, R> {
    private Either() {
    }

    public interface Visitor<L, R, V> {
        public V left(L l);
        public V right(R r);
    }

    public <V> V visit(Visitor<L, R, V> visitor) {
        return accept(visitor::left, visitor::right);
    }

    public abstract <V> V accept(Function<L, V> onLeft, Function<R, V> onRight);

    public static <L, R> Either<L, R> left(L l) {
        return new Either<L, R>() {
            @Override
            public <V> V accept(Function<L, V> onLeft, Function<R, V> onRight) {
                return onLeft.apply(l);
            }
        };
    }

    public static <L, R> Either<L, R> right(R r) {
        return new Either<L, R>() {
            @Override
            public <V> V accept(Function<L, V> onLeft, Function<R, V> onRight) {
                return onRight.apply(r);
            }
        };
    }

    public L leftOrNull() {
        return accept(l -> l, r -> null);
    }

    public R rightOrNull() {
        return accept(l -> null, r -> r);
    }

    public boolean isLeft() {
        return accept(l -> true, r -> false);
    }

    public boolean isRight() {
        return accept(l -> false, r -> true);
    }

    @Override
    public int hashCode() {
        return accept(l -> Objects.hashCode(true, l), r -> Objects.hashCode(false, r));
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Either)) {
            return false;
        }
        Either<?, ?> other = (Either<?, ?>)obj;
        return accept(l -> other.accept(l2 -> Objects.equal(l, l2), r2 -> false), r -> other.accept(l2 -> false, r2 -> Objects.equal(r, r2)));
    }

    public <L2, R2> Either<L2, R2> transform(Function<L, L2> leftFunction, Function<R, R2> rightFunction) {
        return accept(l -> Either.<L2, R2>left(leftFunction.apply(l)), r -> Either.<L2, R2>right(rightFunction.apply(r)));
    }

    public <L2> Either<L2, R> transformLeft(Function<L, L2> function) {
        return transform(function, Functions.<R>identity());
    }

    public <R2> Either<L, R2> transformRight(Function<R, R2> function) {
        return transform(Functions.<L>identity(), function);
    }
}
