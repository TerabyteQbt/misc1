package misc1.commons.ds;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
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
    private final int keyHashCode;
    private final int nodeSize;
    private final int entrySize;
    private final boolean ok;
    private final int height;
    private final ImmutableList<Map.Entry<K, V>> entries;

    private static class HashCodeStats {
        public final int v;
        public final int m;

        private HashCodeStats(int v, int m) {
            this.v = v;
            this.m = m;
        }

        public HashCodeStats(int v) {
            this(v, 31);
        }

        public HashCodeStats() {
            this(0, 1);
        }


        public HashCodeStats merge(HashCodeStats other) {
            return new HashCodeStats(v + m * other.v, m * other.m);
        }
    }

    private final HashCodeStats hashCodeStats;

    private MapNode(MapNode<K, V> left, MapNode<K, V> right, int keyHashCode, K key, V value) {
        this(left, right, keyHashCode, ImmutableList.of(Maps.immutableEntry(key, value)));
    }

    private MapNode(MapNode<K, V> left, MapNode<K, V> right, int keyHashCode, ImmutableList<Map.Entry<K, V>> entries) {
        this.left = left;
        this.right = right;
        this.keyHashCode = keyHashCode;
        this.nodeSize = nodeSizeOf(left) + nodeSizeOf(right) + 1;
        this.entrySize = entrySizeOf(left) + entrySizeOf(right) + entries.size();
        int hl = heightOf(left);
        int hr = heightOf(right);
        this.ok = (left == null || left.ok) && (right == null || right.ok) && (hl <= hr + 1) && (hr <= hl + 1);
        this.height = Math.max(hl, hr) + 1;
        this.entries = entries;

        HashCodeStats hcs = new HashCodeStats();
        hcs = hcs.merge(hashCodeStats(left));
        int nhc = 0;
        for(Map.Entry<K, V> e : entries) {
            nhc += Objects.hashCode(e.getKey()) + Objects.hashCode(e.getValue());
        }
        hcs = hcs.merge(new HashCodeStats(nhc));
        hcs = hcs.merge(hashCodeStats(right));
        this.hashCodeStats = hcs;
    }

    private static <K, V> MapNode<K, V> rotateLeft(MapNode<K, V> node) {
        MapNode<K, V> righty = node.right;
        if(righty == null) {
            throw new IllegalStateException();
        }
        MapNode<K, V> newLeft = new MapNode<K, V>(node.left, righty.left, node.keyHashCode, node.entries);
        return new MapNode<K, V>(newLeft, righty.right, righty.keyHashCode, righty.entries);
    }

    private static <K, V> MapNode<K, V> rotateRight(MapNode<K, V> node) {
        MapNode<K, V> lefty = node.left;
        if(lefty == null) {
            throw new IllegalStateException();
        }
        MapNode<K, V> newRight = new MapNode<K, V>(lefty.right, node.right, node.keyHashCode, node.entries);
        return new MapNode<K, V>(lefty.left, newRight, lefty.keyHashCode, lefty.entries);
    }

    private static final class Removal<K, V> {
        public final MapNode<K, V> newRoot;
        public final int keyHashCode;
        public final ImmutableList<Map.Entry<K, V>> removedEntries;

        public Removal(MapNode<K, V> newRoot, int keyHashCode, ImmutableList<Map.Entry<K, V>> removedEntries) {
            this.newRoot = newRoot;
            this.keyHashCode = keyHashCode;
            this.removedEntries = removedEntries;
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
                MapNode<K, V> newLeft2 = new MapNode<K, V>(newLeft, righty.left, node.keyHashCode, node.entries);
                newNode = new MapNode<K, V>(newLeft2, righty.right, righty.keyHashCode, righty.entries);
            }
            else {
                newNode = new MapNode<K, V>(newLeft, node.right, node.keyHashCode, node.entries);
            }
            return new Removal<K, V>(newNode, removed.keyHashCode, removed.removedEntries);
        }
        return new Removal<K, V>(node.right, node.keyHashCode, node.entries);
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
                MapNode<K, V> newRight2 = new MapNode<K, V>(lefty.right, newRight, node.keyHashCode, node.entries);
                newNode = new MapNode<K, V>(lefty.left, newRight2, lefty.keyHashCode, lefty.entries);
            }
            else {
                newNode = new MapNode<K, V>(node.left, newRight, node.keyHashCode, node.entries);
            }
            return new Removal<K, V>(newNode, removed.keyHashCode, removed.removedEntries);
        }
        return new Removal<K, V>(node.left, node.keyHashCode, node.entries);
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

    public static <K, V> boolean containsKey(MapNode<K, V> node, K key) {
        if(node == null) {
            return false;
        }
        int keyHashCode = Objects.hashCode(key);
        while(node != null) {
            if(keyHashCode < node.keyHashCode) {
                node = node.left;
            }
            else if(keyHashCode > node.keyHashCode) {
                node = node.right;
            }
            else {
                for(Map.Entry<K, V> e : node.entries) {
                    if(Objects.equal(e.getKey(), key)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public static <K, V> boolean containsValue(MapNode<K, V> node, V value) {
        if(node == null) {
            return false;
        }
        for(Map.Entry<K, V> e : node.entries) {
            if(Objects.equal(e.getValue(), value)) {
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
        int keyHashCode = Objects.hashCode(key);
        while(node != null) {
            if(keyHashCode < node.keyHashCode) {
                node = node.left;
            }
            else if(keyHashCode > node.keyHashCode) {
                node = node.right;
            }
            else {
                for(Map.Entry<K, V> e : node.entries) {
                    if(Objects.equal(e.getKey(), key)) {
                        return e.getValue();
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

    public static <K, V> Pair<MapNode<K, V>, V> put(MapNode<K, V> node, int keyHashCode, K key, V value) {
        if(node == null) {
            return Pair.of(new MapNode<K, V>(null, null, keyHashCode, key, value), null);
        }
        if(keyHashCode < node.keyHashCode) {
            Pair<MapNode<K, V>, V> pair = put(node.left, keyHashCode, key, value);
            MapNode<K, V> newNode;
            if(pair.getLeft() == node.left) {
                newNode = node;
            }
            else {
                MapNode<K, V> newLeft = pair.getLeft();
                if(newLeft.height > heightOf(node.right) + 1) {
                    if(keyHashCode > newLeft.keyHashCode) {
                        newLeft = rotateLeft(newLeft);
                    }
                    MapNode<K, V> newRight = new MapNode<K, V>(newLeft.right, node.right, node.keyHashCode, node.entries);
                    newNode = new MapNode<K, V>(newLeft.left, newRight, newLeft.keyHashCode, newLeft.entries);
                    check(newNode);
                }
                else {
                    newNode = new MapNode<K, V>(newLeft, node.right, node.keyHashCode, node.entries);
                    check(newNode);
                }
            }
            return Pair.of(newNode, pair.getRight());
        }
        if(keyHashCode > node.keyHashCode) {
            Pair<MapNode<K, V>, V> pair = put(node.right, keyHashCode, key, value);
            MapNode<K, V> newNode;
            if(pair.getLeft() == node.right) {
                newNode = node;
            }
            else {
                MapNode<K, V> newRight = pair.getLeft();
                if(newRight.height > heightOf(node.left) + 1) {
                    if(keyHashCode < newRight.keyHashCode) {
                        newRight = rotateRight(newRight);
                    }
                    MapNode<K, V> newLeft = new MapNode<K, V>(node.left, newRight.left, node.keyHashCode, node.entries);
                    newNode = new MapNode<K, V>(newLeft, newRight.right, newRight.keyHashCode, newRight.entries);
                    check(newNode);
                }
                else {
                    newNode = new MapNode<K, V>(node.left, newRight, node.keyHashCode, node.entries);
                    check(newNode);
                }
            }
            return Pair.of(newNode, pair.getRight());
        }
        for(int i = 0; i < node.entries.size(); ++i) {
            Map.Entry<K, V> e = node.entries.get(i);
            if(Objects.equal(e.getKey(), key)) {
                if(e.getValue() == value) {
                    return Pair.of(node, value);
                }
                ImmutableList.Builder<Map.Entry<K, V>> b = ImmutableList.builder();
                b.addAll(node.entries.subList(0, i));
                b.add(Maps.immutableEntry(key, value));
                b.addAll(node.entries.subList(i + 1, node.entries.size()));
                return Pair.of(new MapNode<K, V>(node.left, node.right, node.keyHashCode, b.build()), e.getValue());
            }
        }
        ImmutableList.Builder<Map.Entry<K, V>> b = ImmutableList.builder();
        b.addAll(node.entries);
        b.add(Maps.immutableEntry(key, value));
        return Pair.of(new MapNode<K, V>(node.left, node.right, node.keyHashCode, b.build()), null);
    }

    public static <K, V> Pair<MapNode<K, V>, V> remove(MapNode<K, V> node, Object key) {
        Pair<MapNode<K, V>, V> r = remove(node, Objects.hashCode(key), key);
        check(r.getLeft());
        return r;
    }

    public static <K, V> Pair<MapNode<K, V>, V> remove(MapNode<K, V> node, int keyHashCode, Object key) {
        if(node == null) {
            return Pair.of(null, null);
        }
        if(keyHashCode < node.keyHashCode) {
            Pair<MapNode<K, V>, V> pair = remove(node.left, keyHashCode, key);
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
                    MapNode<K, V> newLeft2 = new MapNode<K, V>(newLeft, righty.left, node.keyHashCode, node.entries);
                    newNode = new MapNode<K, V>(newLeft2, righty.right, righty.keyHashCode, righty.entries);
                    check(newNode);
                }
                else {
                    newNode = new MapNode<K, V>(newLeft, node.right, node.keyHashCode, node.entries);
                    check(newNode);
                }
            }
            return Pair.of(newNode, pair.getRight());
        }
        if(keyHashCode > node.keyHashCode) {
            Pair<MapNode<K, V>, V> pair = remove(node.right, keyHashCode, key);
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
                    MapNode<K, V> newRight2 = new MapNode<K, V>(lefty.right, newRight, node.keyHashCode, node.entries);
                    newNode = new MapNode<K, V>(lefty.left, newRight2, lefty.keyHashCode, lefty.entries);
                    check(newNode);
                }
                else {
                    newNode = new MapNode<K, V>(node.left, newRight, node.keyHashCode, node.entries);
                    check(newNode);
                }
            }
            return Pair.of(newNode, pair.getRight());
        }
        for(int i = 0; i < node.entries.size(); ++i) {
            Map.Entry<K, V> e = node.entries.get(i);
            if(Objects.equal(e.getKey(), key)) {
                V value = e.getValue();
                if(node.entries.size() == 1) {
                    if(nodeSizeOf(node.right) > nodeSizeOf(node.left)) {
                        Removal<K, V> removed = removeLeftNode(node.right);
                        return Pair.of(new MapNode<K, V>(node.left, removed.newRoot, removed.keyHashCode, removed.removedEntries), value);
                    }
                    else {
                        if(node.left == null) {
                            return Pair.of(null, value);
                        }
                        else {
                            Removal<K, V> removed = removeRightNode(node.left);
                            return Pair.of(new MapNode<K, V>(removed.newRoot, node.right, removed.keyHashCode, removed.removedEntries), value);
                        }
                    }
                }
                ImmutableList.Builder<Map.Entry<K, V>> b = ImmutableList.builder();
                b.addAll(node.entries.subList(0, i));
                b.addAll(node.entries.subList(i + 1, node.entries.size()));
                return Pair.of(new MapNode<K, V>(node.left, node.right, node.keyHashCode, b.build()), value);
            }
        }
        return Pair.of(node, null);
    }

    private static abstract class DepthFirstIterator<K, V, R> implements Iterator<R> {
        private final Deque<Either<MapNode<K, V>, R>> pending = Lists.newLinkedList();

        public DepthFirstIterator(MapNode<K, V> root) {
            if(root != null) {
                pending.add(Either.<MapNode<K, V>, R>left(root));
            }
        }

        @Override
        public boolean hasNext() {
            return !pending.isEmpty();
        }

        @Override
        public R next() {
            while(true) {
                if(pending.isEmpty()) {
                    throw new NoSuchElementException();
                }
                R next = pending.removeFirst().visit(new Either.Visitor<MapNode<K, V>, R, R>() {
                    @Override
                    public R left(MapNode<K, V> node) {
                        if(node.right != null) {
                            pending.addFirst(Either.<MapNode<K, V>, R>left(node.right));
                        }
                        ImmutableList.Builder<R> b = ImmutableList.builder();
                        mapNode(b, node);
                        List<R> mappedNode = b.build();
                        for(int i = mappedNode.size() - 1; i >= 0; --i) {
                            pending.addFirst(Either.<MapNode<K, V>, R>right(mappedNode.get(i)));
                        }
                        if(node.left != null) {
                            pending.addFirst(Either.<MapNode<K, V>, R>left(node.left));
                        }
                        return null;
                    }

                    @Override
                    public R right(R r) {
                        return r;
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

        protected abstract void mapNode(ImmutableList.Builder<R> b, MapNode<K, V> node);
    }

    public static <K, V> Collection<Map.Entry<K, V>> entries(final MapNode<K, V> root) {
        return new AbstractCollection<Map.Entry<K, V>>() {
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new DepthFirstIterator<K, V, Map.Entry<K, V>>(root) {
                    @Override
                    protected void mapNode(ImmutableList.Builder<Map.Entry<K, V>> b, MapNode<K, V> node) {
                        b.addAll(node.entries);
                    }
                };
            }

            @Override
            public int size() {
                return entrySizeOf(root);
            }
        };
    }

    public static <K, V> int hashCode(MapNode<K, V> root) {
        return hashCodeStats(root).v;
    }

    private static <K, V> HashCodeStats hashCodeStats(MapNode<K, V> root) {
        if(root == null) {
            return new HashCodeStats();
        }
        return root.hashCodeStats;
    }

    private static <K, V> Iterator<MapNode<K, V>> nodesIterator(final MapNode<K, V> root) {
        return new DepthFirstIterator<K, V, MapNode<K, V>>(root) {
            @Override
            protected void mapNode(ImmutableList.Builder<MapNode<K, V>> b, MapNode<K, V> node) {
                b.add(node);
            }
        };
    }

    public static <K, V> boolean equals(MapNode<K, V> root1, MapNode<K, V> root2) {
        Iterator<MapNode<K, V>> ni1 = nodesIterator(root1);
        Iterator<MapNode<K, V>> ni2 = nodesIterator(root2);

        while(true) {
            if(ni1.hasNext()) {
                if(ni2.hasNext()) {
                    // great
                }
                else {
                    return false;
                }
            }
            else {
                if(ni2.hasNext()) {
                    return false;
                }
                else {
                    // even greater
                    return true;
                }
            }
            MapNode<K, V> n1 = ni1.next();
            MapNode<K, V> n2 = ni2.next();
            if(n1.entries.size() != n2.entries.size()) {
                return false;
            }
            int sz = n1.entries.size();
            if(!checkSubset(n1, n2, sz)) {
                return false;
            }
            if(!checkSubset(n2, n1, sz)) {
                return false;
            }
        }
    }

    private static <K, V> boolean checkSubset(MapNode<K, V> n1, MapNode<K, V> n2, int sz) {
        for(int i = 0; i < sz; ++i) {
            Map.Entry<K, V> e1 = n1.entries.get(i);
            boolean found = false;
            for(int j = 0; j < sz; ++j) {
                Map.Entry<K, V> e2 = n2.entries.get(j);
                if(Objects.equal(e1.getKey(), e2.getKey())) {
                    if(Objects.equal(e1.getValue(), e2.getValue())) {
                        found = true;
                        break;
                    }
                    return false;
                }
            }
            if(!found) {
                return false;
            }
        }
        return true;
    }
}
