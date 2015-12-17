package misc1.commons.ds;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractCollection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import misc1.commons.Either;
import org.apache.commons.lang3.tuple.Pair;

/**
 * An AVL tree node [1].  In particular keys are unique and
 * Math.abs(sizeOf(left) - sizeOf(right)) <= 1 in general (at least in all
 * publicly exposed instances).
 *
 * [1] http://en.wikipedia.org/wiki/AVL_tree
 */
class MapNode<K, V> {
    private final MapNode<K, V> left;
    private final MapNode<K, V> right;
    private final int hashCode;
    private final int nodeSize;
    private final int entrySize;
    private final boolean ok;
    private final int height;
    private final Object[] keys;
    private final Object[] values;

    private MapNode(MapNode<K, V> left, MapNode<K, V> right, int hashCode, K key, V value) {
        this(left, right, hashCode, new Object[] { key }, new Object[] { value });
    }

    private MapNode(MapNode<K, V> left, MapNode<K, V> right, int hashCode, Object[] keys, Object[] values) {
        this.left = left;
        this.right = right;
        this.hashCode = hashCode;
        this.nodeSize = nodeSizeOf(left) + nodeSizeOf(right) + 1;
        this.entrySize = entrySizeOf(left) + entrySizeOf(right) + keys.length;
        int hl = heightOf(left);
        int hr = heightOf(right);
        this.ok = (left == null || left.ok) && (right == null || right.ok) && (hl <= hr + 1) && (hr <= hl + 1);
        this.height = Math.max(hl, hr) + 1;
        this.keys = keys;
        this.values = values;
    }

    private static <K, V> MapNode<K, V> rotateLeft(MapNode<K, V> node) {
        MapNode<K, V> righty = node.right;
        if(righty == null) {
            throw new IllegalStateException();
        }
        MapNode<K, V> newLeft = new MapNode<K, V>(node.left, righty.left, node.hashCode, node.keys, node.values);
        return new MapNode<K, V>(newLeft, righty.right, righty.hashCode, righty.keys, righty.values);
    }

    private static <K, V> MapNode<K, V> rotateRight(MapNode<K, V> node) {
        MapNode<K, V> lefty = node.left;
        if(lefty == null) {
            throw new IllegalStateException();
        }
        MapNode<K, V> newRight = new MapNode<K, V>(lefty.right, node.right, node.hashCode, node.keys, node.values);
        return new MapNode<K, V>(lefty.left, newRight, lefty.hashCode, lefty.keys, lefty.values);
    }

    private static final class Removal<K, V> {
        public final MapNode<K, V> newRoot;
        public final int hashCode;
        public final Object[] removedKeys;
        public final Object[] removedValues;

        public Removal(MapNode<K, V> newRoot, int hashCode, Object[] removedKeys, Object[] removedValues) {
            this.newRoot = newRoot;
            this.hashCode = hashCode;
            this.removedKeys = removedKeys;
            this.removedValues = removedValues;
        }
    }

    private static <K, V> Removal<K, V> removeLeftNode(MapNode<K, V> node) {
        if(node == null) {
            throw new IllegalStateException();
        }
        if(node.left != null) {
            Removal<K, V> removed = removeLeftNode(node.left);
            MapNode<K, V> newNode;
            MapNode<K, V> newLeft = removed.newRoot;
            if(heightOf(newLeft) + 1 < heightOf(node.right)) {
                MapNode<K, V> righty = node.right;
                if(heightOf(righty.left) > heightOf(righty.right)) {
                    righty = rotateRight(righty);
                }
                MapNode<K, V> newLeft2 = new MapNode<K, V>(newLeft, righty.left, node.hashCode, node.keys, node.values);
                newNode = new MapNode<K, V>(newLeft2, righty.right, righty.hashCode, righty.keys, righty.values);
            }
            else {
                newNode = new MapNode<K, V>(newLeft, node.right, node.hashCode, node.keys, node.values);
            }
            return new Removal<K, V>(newNode, removed.hashCode, removed.removedKeys, removed.removedValues);
        }
        return new Removal<K, V>(node.right, node.hashCode, node.keys, node.values);
    }

    private static <K, V> Removal<K, V> removeRightNode(MapNode<K, V> node) {
        if(node == null) {
            throw new IllegalStateException();
        }
        if(node.right != null) {
            Removal<K, V> removed = removeRightNode(node.right);
            MapNode<K, V> newNode;
            MapNode<K, V> newRight = removed.newRoot;
            if(heightOf(newRight) + 1 < heightOf(node.left)) {
                MapNode<K, V> lefty = node.left;
                if(heightOf(lefty.right) > heightOf(lefty.left)) {
                    lefty = rotateLeft(lefty);
                }
                MapNode<K, V> newRight2 = new MapNode<K, V>(lefty.right, newRight, node.hashCode, node.keys, node.values);
                newNode = new MapNode<K, V>(lefty.left, newRight2, lefty.hashCode, lefty.keys, lefty.values);
            }
            else {
                newNode = new MapNode<K, V>(node.left, newRight, node.hashCode, node.keys, node.values);
            }
            return new Removal<K, V>(newNode, removed.hashCode, removed.removedKeys, removed.removedValues);
        }
        return new Removal<K, V>(node.left, node.hashCode, node.keys, node.values);
    }

    private static int heightOf(MapNode<?, ?> node) {
        return node == null ? 0 : node.height;
    }

    private static int nodeSizeOf(MapNode<?, ?> node) {
        return node == null ? 0 : node.nodeSize;
    }

    public static int entrySizeOf(MapNode<?, ?> node) {
        return node == null ? 0 : node.entrySize;
    }

    public static boolean containsKey(MapNode<?, ?> node, Object key) {
        if(node == null) {
            return false;
        }
        int hashCode = Objects.hashCode(key);
        while(node != null) {
            if(hashCode < node.hashCode) {
                node = node.left;
            }
            else if(hashCode > node.hashCode) {
                node = node.right;
            }
            else {
                for(int i = 0; i < node.keys.length; ++i) {
                    if(Objects.equal(node.keys[i], key)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public static boolean containsValue(MapNode<?, ?> node, Object value) {
        if(node == null) {
            return false;
        }
        for(int i = 0; i < node.keys.length; ++i) {
            if(Objects.equal(node.values[i], value)) {
                return true;
            }
        }
        if(containsValue(node.left, value)) {
            return true;
        }
        if(containsValue(node.right, value)) {
            return true;
        }
        return false;
    }

    public static <K, V> V get(MapNode<K, V> node, Object key) {
        int hashCode = Objects.hashCode(key);
        while(node != null) {
            if(hashCode < node.hashCode) {
                node = node.left;
            }
            else if(hashCode > node.hashCode) {
                node = node.right;
            }
            else {
                for(int i = 0; i < node.keys.length; ++i) {
                    if(Objects.equal(node.keys[i], key)) {
                        return node.value(i);
                    }
                }
                return null;
            }
        }
        return null;
    }

    private static void check(MapNode<?, ?> n) {
        if(n != null && !n.ok) {
            throw new IllegalStateException();
        }
    }

    public static <K, V> Pair<MapNode<K, V>, V> put(MapNode<K, V> node, K key, V value) {
        Pair<MapNode<K, V>, V> r = put(node, Objects.hashCode(key), key, value);
        check(r.getLeft());
        return r;
    }

    public static <K, V> Pair<MapNode<K, V>, V> put(MapNode<K, V> node, int hashCode, K key, V value) {
        if(node == null) {
            return Pair.of(new MapNode<K, V>(null, null, hashCode, key, value), null);
        }
        if(hashCode < node.hashCode) {
            Pair<MapNode<K, V>, V> pair = put(node.left, hashCode, key, value);
            MapNode<K, V> newNode;
            if(pair.getLeft() == node.left) {
                newNode = node;
            }
            else {
                MapNode<K, V> newLeft = pair.getLeft();
                if(newLeft.height > heightOf(node.right) + 1) {
                    if(hashCode > newLeft.hashCode) {
                        newLeft = rotateLeft(newLeft);
                    }
                    MapNode<K, V> newRight = new MapNode<K, V>(newLeft.right, node.right, node.hashCode, node.keys, node.values);
                    newNode = new MapNode<K, V>(newLeft.left, newRight, newLeft.hashCode, newLeft.keys, newLeft.values);
                    check(newNode);
                }
                else {
                    newNode = new MapNode<K, V>(newLeft, node.right, node.hashCode, node.keys, node.values);
                    check(newNode);
                }
            }
            return Pair.of(newNode, pair.getRight());
        }
        if(hashCode > node.hashCode) {
            Pair<MapNode<K, V>, V> pair = put(node.right, hashCode, key, value);
            MapNode<K, V> newNode;
            if(pair.getLeft() == node.right) {
                newNode = node;
            }
            else {
                MapNode<K, V> newRight = pair.getLeft();
                if(newRight.height > heightOf(node.left) + 1) {
                    if(hashCode < newRight.hashCode) {
                        newRight = rotateRight(newRight);
                    }
                    MapNode<K, V> newLeft = new MapNode<K, V>(node.left, newRight.left, node.hashCode, node.keys, node.values);
                    newNode = new MapNode<K, V>(newLeft, newRight.right, newRight.hashCode, newRight.keys, newRight.values);
                    check(newNode);
                }
                else {
                    newNode = new MapNode<K, V>(node.left, newRight, node.hashCode, node.keys, node.values);
                    check(newNode);
                }
            }
            return Pair.of(newNode, pair.getRight());
        }
        for(int i = 0; i < node.keys.length; ++i) {
            if(Objects.equal(node.keys[i], key)) {
                if(node.values[i] == value) {
                    return Pair.of(node, value);
                }
                Object[] newKeys = new Object[node.keys.length];
                System.arraycopy(node.keys, 0, newKeys, 0, node.keys.length);
                Object[] newValues = new Object[node.keys.length];
                System.arraycopy(node.values, 0, newValues, 0, node.keys.length);
                newValues[i] = value;
                return Pair.of(new MapNode<K, V>(node.left, node.right, node.hashCode, newKeys, newValues), node.value(i));
            }
        }
        Object[] newKeys = new Object[node.keys.length + 1];
        System.arraycopy(node.keys, 0, newKeys, 0, node.keys.length);
        newKeys[node.keys.length] = key;
        Object[] newValues = new Object[node.keys.length + 1];
        System.arraycopy(node.values, 0, newValues, 0, node.keys.length);
        newValues[node.keys.length] = value;
        return Pair.of(new MapNode<K, V>(node.left, node.right, node.hashCode, newKeys, newValues), null);
    }

    public static <K, V> Pair<MapNode<K, V>, V> remove(MapNode<K, V> node, Object key) {
        Pair<MapNode<K, V>, V> r = remove(node, Objects.hashCode(key), key);
        check(r.getLeft());
        return r;
    }

    public static <K, V> Pair<MapNode<K, V>, V> remove(MapNode<K, V> node, int hashCode, Object key) {
        if(node == null) {
            return Pair.of(null, null);
        }
        if(hashCode < node.hashCode) {
            Pair<MapNode<K, V>, V> pair = remove(node.left, hashCode, key);
            MapNode<K, V> newNode;
            if(pair.getLeft() == node.left) {
                newNode = node;
            }
            else {
                MapNode<K, V> newLeft = pair.getLeft();
                if(heightOf(newLeft) + 1 < heightOf(node.right)) {
                    MapNode<K, V> righty = node.right;
                    if(heightOf(righty.left) > heightOf(righty.right)) {
                        righty = rotateRight(righty);
                    }
                    MapNode<K, V> newLeft2 = new MapNode<K, V>(newLeft, righty.left, node.hashCode, node.keys, node.values);
                    newNode = new MapNode<K, V>(newLeft2, righty.right, righty.hashCode, righty.keys, righty.values);
                    check(newNode);
                }
                else {
                    newNode = new MapNode<K, V>(newLeft, node.right, node.hashCode, node.keys, node.values);
                    check(newNode);
                }
            }
            return Pair.of(newNode, pair.getRight());
        }
        if(hashCode > node.hashCode) {
            Pair<MapNode<K, V>, V> pair = remove(node.right, hashCode, key);
            MapNode<K, V> newNode;
            if(pair.getLeft() == node.right) {
                newNode = node;
            }
            else {
                MapNode<K, V> newRight = pair.getLeft();
                if(heightOf(newRight) + 1 < heightOf(node.left)) {
                    MapNode<K, V> lefty = node.left;
                    if(heightOf(lefty.right) > heightOf(lefty.left)) {
                        lefty = rotateLeft(lefty);
                    }
                    MapNode<K, V> newRight2 = new MapNode<K, V>(lefty.right, newRight, node.hashCode, node.keys, node.values);
                    newNode = new MapNode<K, V>(lefty.left, newRight2, lefty.hashCode, lefty.keys, lefty.values);
                    check(newNode);
                }
                else {
                    newNode = new MapNode<K, V>(node.left, newRight, node.hashCode, node.keys, node.values);
                    check(newNode);
                }
            }
            return Pair.of(newNode, pair.getRight());
        }
        for(int i = 0; i < node.keys.length; ++i) {
            if(Objects.equal(node.keys[i], key)) {
                V value = node.value(i);
                if(node.keys.length == 1) {
                    if(nodeSizeOf(node.right) > nodeSizeOf(node.left)) {
                        Removal<K, V> removed = removeLeftNode(node.right);
                        return Pair.of(new MapNode<K, V>(node.left, removed.newRoot, removed.hashCode, removed.removedKeys, removed.removedValues), value);
                    }
                    else {
                        if(node.left == null) {
                            return Pair.of(null, value);
                        }
                        else {
                            Removal<K, V> removed = removeRightNode(node.left);
                            return Pair.of(new MapNode<K, V>(removed.newRoot, node.right, removed.hashCode, removed.removedKeys, removed.removedValues), value);
                        }
                    }
                }
                Object[] newKeys = new Object[node.keys.length - 1];
                System.arraycopy(node.keys, 0, newKeys, 0, i);
                System.arraycopy(node.keys, i + 1, newKeys, i, node.keys.length - i - 1);
                Object[] newValues = new Object[node.keys.length - 1];
                System.arraycopy(node.values, 0, newValues, 0, i);
                System.arraycopy(node.values, i + 1, newValues, i, node.keys.length - i - 1);
                return Pair.of(new MapNode<K, V>(node.left, node.right, node.hashCode, newKeys, newValues), value);
            }
        }
        return Pair.of(node, null);
    }

    public AbstractCollection<Map.Entry<K, V>> entries() {
        return new AbstractCollection<Map.Entry<K, V>>() {
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                final Deque<Either<MapNode<K, V>, Map.Entry<K, V>>> pending = Lists.newLinkedList();
                pending.add(Either.<MapNode<K, V>, Map.Entry<K, V>>left(MapNode.this));
                return new Iterator<Map.Entry<K, V>>() {
                    @Override
                    public boolean hasNext() {
                        return !pending.isEmpty();
                    }

                    @Override
                    public Map.Entry<K, V> next() {
                        while(true) {
                            if(pending.isEmpty()) {
                                throw new NoSuchElementException();
                            }
                            Map.Entry<K, V> next = pending.removeFirst().visit(new Either.Visitor<MapNode<K, V>, Map.Entry<K, V>, Map.Entry<K, V>>() {
                                @Override
                                public Map.Entry<K, V> left(MapNode<K, V> node) {
                                    if(node.right != null) {
                                        pending.addFirst(Either.<MapNode<K, V>, Map.Entry<K, V>>left(node.right));
                                    }
                                    for(int i = node.keys.length - 1; i >= 0; --i) {
                                        pending.addFirst(Either.<MapNode<K, V>, Map.Entry<K, V>>right(Maps.immutableEntry(node.key(i), node.value(i))));
                                    }
                                    if(node.left != null) {
                                        pending.addFirst(Either.<MapNode<K, V>, Map.Entry<K, V>>left(node.left));
                                    }
                                    return null;
                                }

                                @Override
                                public Map.Entry<K, V> right(Map.Entry<K, V> entry) {
                                    return entry;
                                }
                            });
                            if(next != null) {
                                return next;
                            }
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size() {
                return entrySize;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private K key(int i) {
        return (K) keys[i];
    }

    @SuppressWarnings("unchecked")
    private V value(int i) {
        return (V) values[i];
    }
}
