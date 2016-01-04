package misc1.commons.concurrent.ctree;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.Map;
import misc1.commons.tuple.Misc1PairUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

public final class ComputationTree<V> {
    // hidden
    private ComputationTree(ImmutableList<ComputationTree<?>> children, Function<ImmutableList<Object>, V> postProcess) {
        this.children = children;
        this.postProcess = postProcess;
    }

    final ImmutableList<ComputationTree<?>> children;
    final Function<ImmutableList<Object>, V> postProcess;

    public static ComputationTree<ObjectUtils.Null> constant() {
        return constant(ObjectUtils.NULL);
    }

    public static <V> ComputationTree<V> constant(final V v) {
        return new ComputationTree<V>(ImmutableList.<ComputationTree<?>>of(), (input) -> v);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getElementTyped(ImmutableList<Object> list, int index) {
        return (T)list.get(index);
    }

    public static <A, B> ComputationTree<Pair<A, B>> pair(ComputationTree<A> lhs, ComputationTree<B> rhs) {
        return new ComputationTree<Pair<A, B>>(ImmutableList.of(lhs, rhs), (input) -> {
            A lhsInner = getElementTyped(input, 0);
            B rhsInner = getElementTyped(input, 1);
            return Pair.of(lhsInner, rhsInner);
        });
    }

    public static <V> ComputationTree<ImmutableList<V>> list(Iterable<ComputationTree<V>> children) {
        return new ComputationTree<ImmutableList<V>>(ImmutableList.<ComputationTree<?>>copyOf(children), (input) -> (ImmutableList<V>)input);
    }

    public static <K, V> ComputationTree<ImmutableMap<K, V>> map(Map<K, ComputationTree<V>> map) {
        return list(Iterables.transform(map.entrySet(), (input) -> {
            final K k = input.getKey();
            return input.getValue().transform((input2) -> Pair.of(k, input2));
        })).transform((input) -> {
            ImmutableMap.Builder<K, V> b = ImmutableMap.builder();
            for(Pair<K, V> p : input) {
                b.put(p);
            }
            return b.build();
        });
    }

    public <W> ComputationTree<W> transform(final Function<? super V, W> fn) {
        return new ComputationTree<W>(ImmutableList.<ComputationTree<?>>of(this), (input) -> {
            V v = getElementTyped(input, 0);
            return fn.apply(v);
        });
    }

    public static <V, W> ComputationTree<ImmutableList<W>> transformIterable(Iterable<V> inputs, Function<V, W> fn) {
        ImmutableList.Builder<ComputationTree<W>> outputsBuilder = ImmutableList.builder();
        for(V input : inputs) {
            outputsBuilder.add(ComputationTree.constant(input).transform(fn));
        }
        return list(outputsBuilder.build());
    }

    public ComputationTree<ObjectUtils.Null> ignore() {
        return transform(Functions.constant(ObjectUtils.NULL));
    }

    public <W> ComputationTree<V> combineLeft(ComputationTree<W> right) {
        return ComputationTree.pair(this, right).transform(Misc1PairUtils.<V, W>leftFunction());
    }

    public <W> ComputationTree<W> combineRight(ComputationTree<W> right) {
        return ComputationTree.pair(this, right).transform(Misc1PairUtils.<V, W>rightFunction());
    }

    public static ComputationTree<Boolean> and(Iterable<ComputationTree<Boolean>> inputs) {
        return ComputationTree.list(inputs).transform((input) -> {
            for(Boolean b : input) {
                if(!b) {
                    return false;
                }
            }
            return true;
        });
    }
}
