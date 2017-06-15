package misc1.commons.ds.union;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public abstract class Union<U extends Union<U>> {
    private final UnionType<U> type;
    protected final UnionChoice<U, ?> choice;

    protected Union(UnionType<U> type, UnionChoice<U, ?> choice) {
        this.type = type;
        this.choice = choice;
    }

    protected abstract U self();

    @Override
    public int hashCode() {
        return Objects.hashCode(choice);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!getClass().equals(obj.getClass())) {
            return false;
        }
        Union<U> other = (Union<U>)obj;
        return choice.equals(other.choice);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + choice + "}";
    }

    public <R> UnionVisit<U, R> visit() {
        return new UnionVisit<U, R>() {
            @Override
            public <T> UnionVisit<U, R> on(UnionKey<U, T> key, Function<T, R> fn) {
                Optional<T> opt = choice.optional(key);
                if(opt.isPresent()) {
                    R ret = fn.apply(opt.get());
                    return new UnionVisit<U, R>() {
                        @Override
                        public <T> UnionVisit<U, R> on(UnionKey<U, T> key, Function<T, R> fn) {
                            return this;
                        }

                        @Override
                        public R complete(Function<U, R> def) {
                            return ret;
                        }
                    };
                }
                return this;
            }

            @Override
            public R complete(Function<U, R> def) {
                return def.apply(self());
            }
        };
    }
}
