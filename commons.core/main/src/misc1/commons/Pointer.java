package misc1.commons;

public final class Pointer<V> {
    public final V value;

    public Pointer(V value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Pointer)) {
            return false;
        }
        Pointer<?> other = (Pointer<?>)obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        return "Pointer(" + value + ")";
    }

    public static <V> Pointer<V> of(V value) {
        return new Pointer<V>(value);
    }
}
