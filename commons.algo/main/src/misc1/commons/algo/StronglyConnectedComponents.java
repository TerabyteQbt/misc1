package misc1.commons.algo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class StronglyConnectedComponents<V> {
    private final Map<V, Component<V>> components = Maps.newHashMap();

    public static class Component<V> {
        public final ImmutableList<V> vertices;

        private Component(ImmutableList<V> vertices) {
            this.vertices = vertices;
        }
    }

    private static class Stack<V> {
        private final Deque<V> list = Lists.newLinkedList();
        private final Set<V> set = Sets.newHashSet();

        public void push(V v) {
            list.addLast(v);
            set.add(v);
        }

        public V pop() {
            V v = list.removeLast();
            set.remove(v);
            return v;
        }

        public boolean contains(V v) {
            return set.contains(v);
        }
    }

    private class Pass {
        private final Stack<V> stack = new Stack<V>();
        private final Map<V, Integer> indices = Maps.newHashMap();
        private int index = 0;

        public int search(V v) {
            // already completed, nothing can help the search for things upstack
            if(components.containsKey(v)) {
                return Integer.MAX_VALUE;
            }

            // on the stack, don't reenter, this counts exactly as a link back to that vertex
            if(stack.contains(v)) {
                return indices.get(v);
            }

            // never been seen before, start searching...

            int vIndex = index++;
            indices.put(v, vIndex);
            stack.push(v);

            int vLow = vIndex;
            for(V w : getLinks(v)) {
                int wLow = search(w);
                if(wLow < vLow) {
                    vLow = wLow;
                }
            }

            if(vIndex == vLow) {
                ImmutableList.Builder<V> b = ImmutableList.builder();
                while(true) {
                    V w = stack.pop();
                    b.add(w);
                    if(w.equals(v)) {
                        break;
                    }
                }
                Component<V> c = new Component<V>(b.build());
                for(V w : c.vertices) {
                    components.put(w, c);
                }
            }

            return vLow;
        }
    }

    public Component<V> compute(V v) {
        new Pass().search(v);
        return components.get(v);
    }

    public List<Component<V>> getLinks(Component<V> c) {
        ImmutableSet.Builder<Component<V>> b = ImmutableSet.builder();
        for(V v : c.vertices) {
            for(V w : getLinks(v)) {
                Component<V> c2 = compute(w);
                if(c == c2) {
                    continue;
                }
                b.add(c2);
            }
        }
        return ImmutableList.copyOf(b.build());
    }

    protected abstract Iterable<V> getLinks(V v);
}
