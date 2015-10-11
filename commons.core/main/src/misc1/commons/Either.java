package misc1.commons;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;

public abstract class Either<L, R> {
    protected final boolean isLeft;
    protected final L l;
    protected final R r;

    private Either(boolean isLeft, L l, R r) {
        this.isLeft = isLeft;
        this.l = l;
        this.r = r;
    }

    public interface Visitor<L, R, V> {
        public V left(L l);
        public V right(R r);
    }

    public abstract <V> V visit(Visitor<L, R, V> visitor);

    private static final class EitherLeft<L, R> extends Either<L, R> {
        public EitherLeft(L l) {
            super(true, l, null);
        }

        @Override
        public <V> V visit(misc1.commons.Either.Visitor<L, R, V> visitor) {
            return visitor.left(l);
        }

        @Override
        public <L2, R2> Either<L2, R2> transform(Function<L, L2> leftFunction, Function<R, R2> rightFunction) {
            return new EitherLeft<L2, R2>(leftFunction.apply(l));
        }

        @Override
        public String toString() {
            return "EitherLeft(" + l + ")";
        }
    }

    public static <L, R> Either<L, R> left(L l) {
        return new EitherLeft<L, R>(l);
    }

    private static final class EitherRight<L, R> extends Either<L, R> {
        public EitherRight(R r) {
            super(false, null, r);
        }

        @Override
        public <V> V visit(misc1.commons.Either.Visitor<L, R, V> visitor) {
            return visitor.right(r);
        }

        @Override
        public <L2, R2> Either<L2, R2> transform(Function<L, L2> leftFunction, Function<R, R2> rightFunction) {
            return new EitherRight<L2, R2>(rightFunction.apply(r));
        }

        @Override
        public String toString() {
            return "EitherRight(" + r + ")";
        }
    }

    public static <L, R> Either<L, R> right(R r) {
        return new EitherRight<L, R>(r);
    }

    public L leftOrNull() {
        return l;
    }

    public R rightOrNull() {
        return r;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public boolean isRight() {
        return !isLeft;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isLeft, l, r);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Either)) {
            return false;
        }
        Either<?, ?> other = (Either<?, ?>)obj;
        if(isLeft != other.isLeft) {
            return false;
        }
        if(!Objects.equal(l, other.l)) {
            return false;
        }
        if(!Objects.equal(r, other.r)) {
            return false;
        }
        return true;
    }

    public abstract <L2, R2> Either<L2, R2> transform(Function<L, L2> leftFunction, Function<R, R2> rightFunction);

    public <L2> Either<L2, R> transformLeft(Function<L, L2> function) {
        return transform(function, Functions.<R>identity());
    }

    public <R2> Either<L, R2> transformRight(Function<R, R2> function) {
        return transform(Functions.<L>identity(), function);
    }
}
