package misc1.commons.ds;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;

public class ImmutableSalvagingMultimap<K, V> {
    private final ImmutableSalvagingMap<K, ImmutableSalvagingSet<V>> keyMap;
    private final ImmutableSalvagingMap<V, ImmutableSalvagingSet<K>> valueMap;

    public ImmutableSalvagingMultimap() {
        this.keyMap = ImmutableSalvagingMap.of();
        this.valueMap = ImmutableSalvagingMap.of();
    }

    private ImmutableSalvagingMultimap(ImmutableSalvagingMap<K, ImmutableSalvagingSet<V>> keyMap, ImmutableSalvagingMap<V, ImmutableSalvagingSet<K>> valueMap) {
        this.keyMap = keyMap;
        this.valueMap = valueMap;
    }

    public boolean isEmpty() {
        return keyMap.isEmpty();
    }

    public boolean containsKey(K key) {
        return keyMap.containsKey(key);
    }

    public boolean containsValue(V value) {
        return valueMap.containsKey(value);
    }

    public boolean containsEntry(K key, V value) {
        return keyMap.containsKey(key) && keyMap.get(key).contains(value);
    }

    public ImmutableSalvagingSet<V> get(K key) {
        return keyMap.get(key);
    }

    public ImmutableSalvagingMultimap<K, V> put(K key, V value) {
        return of(halfPut(keyMap, key, value), halfPut(valueMap, value, key));
    }

    public ImmutableSalvagingMultimap<K, V> removeEntry(K key, V value) {
        return of(halfRemove(keyMap, key, value), halfRemove(valueMap, value, key));
    }

    public Iterable<Map.Entry<K, V>> entries() {
        return Iterables.concat(Iterables.transform(keyMap.entries(), (input) -> {
            return Iterables.transform(input.getValue(), (V v) -> Maps.immutableEntry(input.getKey(), v));
        }));
    }

    public Collection<K> keys() {
        return keyMap.keys();
    }

    public Collection<V> values() {
        return valueMap.keys();
    }

    public ImmutableSalvagingMultimap<V, K> inverse() {
        return of(valueMap, keyMap);
    }

    private static final ImmutableSalvagingMultimap<Object, Object> EMPTY = new ImmutableSalvagingMultimap<Object, Object>();
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <K, V> ImmutableSalvagingMultimap<K, V> of() {
        return (ImmutableSalvagingMultimap) EMPTY;
    }

    @Override
    public int hashCode() {
        return keyMap.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ImmutableSalvagingMultimap)) {
            return false;
        }
        ImmutableSalvagingMultimap<K, V> other = (ImmutableSalvagingMultimap<K, V>)obj;
        return this.keyMap.equals(other.keyMap);
    }

    private <A, B> ImmutableSalvagingMap<A, ImmutableSalvagingSet<B>> halfPut(ImmutableSalvagingMap<A, ImmutableSalvagingSet<B>> map, A a, B b) {
        ImmutableSalvagingSet<B> newSet = map.get(a);
        if(newSet == null) {
            newSet = ImmutableSalvagingSet.of();
        }
        newSet = newSet.add(b);
        return map.simplePut(a, newSet);
    }

    private <A, B> ImmutableSalvagingMap<A, ImmutableSalvagingSet<B>> halfRemove(ImmutableSalvagingMap<A, ImmutableSalvagingSet<B>> map, A a, B b) {
        ImmutableSalvagingSet<B> newSet = map.get(a);
        if(newSet == null) {
            newSet = ImmutableSalvagingSet.of();
        }
        newSet = newSet.remove(b);
        if(newSet.isEmpty()) {
            return map.simpleRemove(a);
        }
        return map.simplePut(a, newSet);
    }

    private static <A, B> ImmutableSalvagingMultimap<A, B> of(ImmutableSalvagingMap<A, ImmutableSalvagingSet<B>> keyMap, ImmutableSalvagingMap<B, ImmutableSalvagingSet<A>> valueMap) {
        return new ImmutableSalvagingMultimap<A, B>(keyMap, valueMap);
    }
}
