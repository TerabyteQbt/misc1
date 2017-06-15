package misc1.commons.ds.union;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

public final class UnionChoice<U extends Union<U>, T> {
    public final UnionKey<U, T> key;
    public final T value;

    public UnionChoice(UnionKey<U, T> key, T value) {
        this.key = key;
        this.value = value;
    }

    public <S> Optional<S> optional(UnionKey<U, S> key2) {
        if(key.equals(key2)) {
            return Optional.of((S)value);
        }
        return Optional.absent();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!(obj instanceof UnionChoice)) {
            return false;
        }
        UnionChoice<?, ?> other = (UnionChoice<?, ?>)obj;
        if(!key.equals(other.key)) {
            return false;
        }
        if(!Objects.equal(value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
