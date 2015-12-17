package misc1.commons.ds;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

public class ImmutableSalvagingSet<T> implements Iterable<T> {
    private final MapNode<T, ObjectUtils.Null> root;

    public ImmutableSalvagingSet() {
        this.root = null;
    }

    private ImmutableSalvagingSet(MapNode<T, ObjectUtils.Null> root) {
        this.root = root;
    }

    public int size() {
        return MapNode.entrySizeOf(root);
    }

    public boolean isEmpty() {
        return root == null;
    }

    public boolean contains(Object key) {
        return MapNode.containsKey(root, key);
    }

    public ImmutableSalvagingSet<T> add(T key) {
        Pair<MapNode<T, ObjectUtils.Null>, ObjectUtils.Null> pair = MapNode.put(root, key, ObjectUtils.NULL);
        return new ImmutableSalvagingSet<T>(pair.getLeft());
    }

    public ImmutableSalvagingSet<T> remove(Object key) {
        Pair<MapNode<T, ObjectUtils.Null>, ObjectUtils.Null> pair = MapNode.remove(root, key);
        return new ImmutableSalvagingSet<T>(pair.getLeft());
    }

    private static Function<Map.Entry<Object, Object>, Object> GET_KEY_FUNCTION = new Function<Map.Entry<Object, Object>, Object>() {
        @Override
        public Object apply(Map.Entry<Object, Object> input) {
            return input.getKey();
        }
    };
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> Function<Map.Entry<T, ObjectUtils.Null>, T> getKeyFunction() {
        return (Function<Map.Entry<T, ObjectUtils.Null>, T>) (Function) GET_KEY_FUNCTION;
    }
    @Override
    public Iterator<T> iterator() {
        return root == null ? Iterators.<T>emptyIterator() : Iterators.transform(root.entries().iterator(), ImmutableSalvagingSet.<T>getKeyFunction());
    }

    public static <T> ImmutableSalvagingSet<T> of() {
        return new ImmutableSalvagingSet<T>();
    }

    public boolean containsAll(Iterable<?> objects) {
        for(Object o : objects) {
            if(!this.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(this == obj) {
            return true;
        }
        if(obj instanceof ImmutableSalvagingSet<?>) {
            if(((ImmutableSalvagingSet<?>) obj).size() != this.size()) {
                return false;
            }
            if(this.containsAll((ImmutableSalvagingSet<?>) obj)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return root.hashCode();
    }
}
