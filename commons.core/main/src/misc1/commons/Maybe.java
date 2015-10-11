package misc1.commons;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import java.io.Serializable;

public abstract class Maybe<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Maybe() {
    }

    private static class MaybeOf<T> extends Maybe<T> {
        private static final long serialVersionUID = 1L;

        private T t;

        public MaybeOf(T t) {
            this.t = t;
        }

        @Override
        public String toString() {
            return "MaybeOf(" + t + ")";
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public T get(T def) {
            return t;
        }

        @Override
        public <T2> Maybe<T2> transform(Function<T, T2> function) {
            return Maybe.of(function.apply(t));
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof MaybeOf)) {
                return false;
            }
            MaybeOf<?> other = (MaybeOf<?>)obj;
            return Objects.equal(t, other.t);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(t);
        }
    }

    private static class MaybeNot<T> extends Maybe<T> {
        private static final long serialVersionUID = 1L;

        @Override
        public String toString() {
            return "MaybeNot()";
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public T get(T def) {
            return def;
        }

        @Override
        public <T2> Maybe<T2> transform(Function<T, T2> function) {
            return Maybe.not();
        }
    }

    private static final Maybe<Object> NOT = new MaybeNot<Object>();
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Maybe<T> not() {
        return (Maybe)NOT;
    }

    private static final Maybe<Object> OF_NULL = Maybe.of(null);
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Maybe<T> of() {
        return (Maybe)OF_NULL;
    }

    public static <T> Maybe<T> of(T t) {
        return new MaybeOf<T>(t);
    }

    public abstract boolean isPresent();
    public abstract T get(T def);
    public abstract <T2> Maybe<T2> transform(Function<T, T2> function);
}
