package misc1.commons.ds.union;

import misc1.commons.merge.Merge;
import misc1.commons.merge.Merges;

public class UnionKey<U extends Union<U>, T> {
    public final String name;

    public UnionKey(String name) {
        this.name = name;
    }

    public Merge<T> merge() {
        return Merges.trivial();
    }

    @Override
    public String toString() {
        return name;
    }
}
