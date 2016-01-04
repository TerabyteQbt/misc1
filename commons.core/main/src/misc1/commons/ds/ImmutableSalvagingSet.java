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

    public boolean contains(T key) {
        return MapNode.containsKey(root, key);
    }

    public ImmutableSalvagingSet<T> add(T key) {
        Pair<MapNode<T, ObjectUtils.Null>, ObjectUtils.Null> pair = MapNode.put(root, key, ObjectUtils.NULL);
        return new ImmutableSalvagingSet<T>(pair.getLeft());
    }

    public ImmutableSalvagingSet<T> remove(T key) {
        Pair<MapNode<T, ObjectUtils.Null>, ObjectUtils.Null> pair = MapNode.remove(root, key);
        return new ImmutableSalvagingSet<T>(pair.getLeft());
    }

    private static Function<Map.Entry<Object, Object>, Object> GET_KEY_FUNCTION = (input) -> input.getKey();
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> Function<Map.Entry<T, ObjectUtils.Null>, T> getKeyFunction() {
        return (Function<Map.Entry<T, ObjectUtils.Null>, T>) (Function) GET_KEY_FUNCTION;
    }
    @Override
    public Iterator<T> iterator() {
        return Iterators.transform(MapNode.entries(root).iterator(), ImmutableSalvagingSet.<T>getKeyFunction());
    }

    public static <T> ImmutableSalvagingSet<T> of() {
        return new ImmutableSalvagingSet<T>();
    }

    @Override
    public int hashCode() {
        return MapNode.hashCode(root);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ImmutableSalvagingSet)) {
            return false;
        }
        ImmutableSalvagingSet<T> other = (ImmutableSalvagingSet<T>)obj;
        return MapNode.equals(root, other.root);
    }
}
