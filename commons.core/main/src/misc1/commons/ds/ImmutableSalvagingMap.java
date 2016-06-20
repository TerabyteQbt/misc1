//   Copyright 2016 Keith Amling
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package misc1.commons.ds;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
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

    public boolean containsKey(K key) {
        return MapNode.containsKey(root, key);
    }

    public boolean containsValue(V value) {
        return MapNode.containsValue(root, value);
    }

    public V get(K key) {
        return MapNode.get(root, key);
    }

    public Pair<ImmutableSalvagingMap<K, V>, V> put(K key, V value) {
        Pair<MapNode<K, V>, V> pair = MapNode.put(root, key, value);
        return Pair.of(new ImmutableSalvagingMap<K, V>(pair.getLeft()), pair.getRight());
    }

    public ImmutableSalvagingMap<K, V> simplePut(K key, V value) {
        return put(key, value).getLeft();
    }

    public Pair<ImmutableSalvagingMap<K, V>, V> remove(K key) {
        Pair<MapNode<K, V>, V> pair = MapNode.remove(root, key);
        return Pair.of(new ImmutableSalvagingMap<K, V>(pair.getLeft()), pair.getRight());
    }

    public ImmutableSalvagingMap<K, V> simpleRemove(K key) {
        return remove(key).getLeft();
    }

    public Iterable<Map.Entry<K, V>> entries() {
        return MapNode.entries(root);
    }

    public ImmutableMap<K, V> toMap() {
        ImmutableMap.Builder<K, V> b = ImmutableMap.builder();
        for(Map.Entry<K, V> e : entries()) {
            b.put(e);
        }
        return b.build();
    }

    public Collection<K> keys() {
        return Collections2.transform(MapNode.entries(root), Map.Entry<K, V>::getKey);
    }

    private static final ImmutableSalvagingMap<Object, Object> EMPTY = new ImmutableSalvagingMap<Object, Object>();
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <K, V> ImmutableSalvagingMap<K, V> of() {
        return (ImmutableSalvagingMap) EMPTY;
    }

    public static <K, V> ImmutableSalvagingMap<K, V> copyOf(Map<K, V> map) {
        ImmutableSalvagingMap<K, V> r = of();
        for(Map.Entry<K, V> e : map.entrySet()) {
            r = r.simplePut(e.getKey(), e.getValue());
        }
        return r;
    }

    @Override
    public int hashCode() {
        return MapNode.hashCode(root);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ImmutableSalvagingMap)) {
            return false;
        }
        ImmutableSalvagingMap<K, V> other = (ImmutableSalvagingMap<K, V>)obj;
        return MapNode.equals(root, other.root);
    }
}
