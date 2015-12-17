package misc1.commons.ds;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public class ImmutableSalvagingMap<K, V> {
    private final MapNode<K, V> root;

    public ImmutableSalvagingMap() {
        this.root = null;
    }

    private ImmutableSalvagingMap(MapNode<K, V> root) {
        this.root = root;
    }

    public int size() {
        return MapNode.entrySizeOf(root);
    }

    public boolean isEmpty() {
        return root == null;
    }

    public boolean containsKey(Object key) {
        return MapNode.containsKey(root, key);
    }

    public boolean containsValue(Object value) {
        return MapNode.containsValue(root, value);
    }

    public V get(Object key) {
        return MapNode.get(root, key);
    }

    public Pair<ImmutableSalvagingMap<K, V>, V> put(K key, V value) {
        Pair<MapNode<K, V>, V> pair = MapNode.put(root, key, value);
        return Pair.of(new ImmutableSalvagingMap<K, V>(pair.getLeft()), pair.getRight());
    }

    public ImmutableSalvagingMap<K, V> simplePut(K key, V value) {
        return put(key, value).getLeft();
    }

    public Pair<ImmutableSalvagingMap<K, V>, V> remove(Object key) {
        Pair<MapNode<K, V>, V> pair = MapNode.remove(root, key);
        return Pair.of(new ImmutableSalvagingMap<K, V>(pair.getLeft()), pair.getRight());
    }

    public ImmutableSalvagingMap<K, V> simpleRemove(Object key) {
        return remove(key).getLeft();
    }

    public Iterable<Map.Entry<K, V>> entries() {
        return root == null ? ImmutableList.<Map.Entry<K, V>>of() : root.entries();
    }

    private static Function<Map.Entry<Object, Object>, Object> GET_KEY_FUNCTION = new Function<Map.Entry<Object, Object>, Object>() {
        @Override
        public Object apply(Map.Entry<Object, Object> input) {
            return input.getKey();
        }
    };
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <K, V> Function<Map.Entry<K, V>, K> getKeyFunction() {
        return (Function<Map.Entry<K, V>, K>) (Function) GET_KEY_FUNCTION;
    }
    public Collection<K> keys() {
        return root == null ? ImmutableList.<K>of() : Collections2.transform(root.entries(), ImmutableSalvagingMap.<K, V>getKeyFunction());
    }

    private static final ImmutableSalvagingMap<Object, Object> EMPTY = new ImmutableSalvagingMap<Object, Object>();
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <K, V> ImmutableSalvagingMap<K, V> of() {
        return (ImmutableSalvagingMap) EMPTY;
    }
}
