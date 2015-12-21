package misc1.commons.merge;

import org.apache.commons.lang3.tuple.Triple;

public interface Merge<V> {
    Triple<V, V, V> merge(V lhs, V mhs, V rhs);
}
