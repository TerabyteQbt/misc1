package misc1.commons.concurrent.ctree;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.Map;
import java.util.function.Supplier;
import misc1.commons.Either;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

public final class ComputationTree<V> {
    // hidden
    private ComputationTree(ImmutableList<ComputationTree<?>> children, PostProcess<V> postProcess) {
        this.children = children;
        this.postProcess = postProcess;
    }

    final ImmutableList<ComputationTree<?>> children;
    final PostProcess<V> postProcess;

    public static ComputationTree<ObjectUtils.Null> constant() {
        return constant(ObjectUtils.NULL);
    }

    public static <V> ComputationTree<V> constant(final V v) {
        return new ComputationTree<V>(ImmutableList.<ComputationTree<?>>of(), (input) -> Either.left(v));
    }

    public static <V> ComputationTree<V> ofSupplier(Supplier<V> fn) {
        return new ComputationTree<V>(ImmutableList.<ComputationTree<?>>of(), (input) -> Either.left(fn.get()));
    }

    @SuppressWarnings("unchecked")
    private static <T> T getElementTyped(ImmutableList<Object> list, int index) {
        return (T)list.get(index);
    }

    public static <A, B> ComputationTree<Pair<A, B>> pair(ComputationTree<A> lhs, ComputationTree<B> rhs) {
        return tuple(lhs, rhs, Pair::of);
    }

    public static <V> ComputationTree<ImmutableList<V>> list(Iterable<ComputationTree<V>> children) {
        return new ComputationTree<ImmutableList<V>>(ImmutableList.<ComputationTree<?>>copyOf(children), (input) -> Either.left((ImmutableList<V>)input));
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
            return Either.left(fn.apply(v));
        });
    }

    public <W> ComputationTree<W> transformExec(Function<V, ComputationTree<W>> fn) {
        return new ComputationTree<W>(ImmutableList.<ComputationTree<?>>of(this), (input) -> {
            V v = getElementTyped(input, 0);
            return Either.right(fn.apply(v));
        });
    }

    public static <V, W> ComputationTree<ImmutableList<W>> transformIterable(Iterable<V> inputs, Function<V, W> fn) {
        return list(Iterables.transform(inputs, (input) -> ComputationTree.constant(input).transform(fn)));
    }

    public static <V, W> ComputationTree<ImmutableList<W>> transformIterableExec(Iterable<V> inputs, Function<V, ComputationTree<W>> fn) {
        return list(Iterables.transform(inputs, (input) -> ComputationTree.constant(input).transformExec(fn)));
    }

    public ComputationTree<ObjectUtils.Null> ignore() {
        return transform(Functions.constant(ObjectUtils.NULL));
    }

    public <W> ComputationTree<V> combineLeft(ComputationTree<W> right) {
        return ComputationTree.tuple(this, right, (l, r) -> l);
    }

    public <W> ComputationTree<W> combineRight(ComputationTree<W> right) {
        return ComputationTree.tuple(this, right, (l, r) -> r);
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

    /*
    my $template = <<EOF;
    public static interface Tuple#Processor<[[V#]], R> {
        R apply([[V# v#]]);
    }
    public static <[[V#]], R> ComputationTree<R> tuple([[ComputationTree<V#> t#]], Tuple#Processor<[[V#]], R> fn) {
        return new ComputationTree<R>(ImmutableList.of([[t#]]), (input) -> {
{{              V# v# = getElementTyped(input, #);
}}              return Either.left(fn.apply([[v#]]));
        });
    }
EOF
    sub r { my ($t, $r) = @_; $t =~ s/#/$r/g; return $t }
    for my $n (2..5) {
        my $txt = $template;
        $txt =~ s/\[\[(.*?)\]\]/join(", ", map { r($1, $_) } (0..($n - 1)))/egs;
        $txt =~ s/\{\{(.*?)\}\}/join("", map { r($1, $_) } (0..($n - 1)))/egs;
        $txt = r($txt, $n);
        print $txt;
    }
    */

    public static interface Tuple2Processor<V0, V1, R> {
        R apply(V0 v0, V1 v1);
    }
    public static <V0, V1, R> ComputationTree<R> tuple(ComputationTree<V0> t0, ComputationTree<V1> t1, Tuple2Processor<V0, V1, R> fn) {
        return new ComputationTree<R>(ImmutableList.of(t0, t1), (input) -> {
              V0 v0 = getElementTyped(input, 0);
              V1 v1 = getElementTyped(input, 1);
              return Either.left(fn.apply(v0, v1));
        });
    }
    public static interface Tuple3Processor<V0, V1, V2, R> {
        R apply(V0 v0, V1 v1, V2 v2);
    }
    public static <V0, V1, V2, R> ComputationTree<R> tuple(ComputationTree<V0> t0, ComputationTree<V1> t1, ComputationTree<V2> t2, Tuple3Processor<V0, V1, V2, R> fn) {
        return new ComputationTree<R>(ImmutableList.of(t0, t1, t2), (input) -> {
              V0 v0 = getElementTyped(input, 0);
              V1 v1 = getElementTyped(input, 1);
              V2 v2 = getElementTyped(input, 2);
              return Either.left(fn.apply(v0, v1, v2));
        });
    }
    public static interface Tuple4Processor<V0, V1, V2, V3, R> {
        R apply(V0 v0, V1 v1, V2 v2, V3 v3);
    }
    public static <V0, V1, V2, V3, R> ComputationTree<R> tuple(ComputationTree<V0> t0, ComputationTree<V1> t1, ComputationTree<V2> t2, ComputationTree<V3> t3, Tuple4Processor<V0, V1, V2, V3, R> fn) {
        return new ComputationTree<R>(ImmutableList.of(t0, t1, t2, t3), (input) -> {
              V0 v0 = getElementTyped(input, 0);
              V1 v1 = getElementTyped(input, 1);
              V2 v2 = getElementTyped(input, 2);
              V3 v3 = getElementTyped(input, 3);
              return Either.left(fn.apply(v0, v1, v2, v3));
        });
    }
    public static interface Tuple5Processor<V0, V1, V2, V3, V4, R> {
        R apply(V0 v0, V1 v1, V2 v2, V3 v3, V4 v4);
    }
    public static <V0, V1, V2, V3, V4, R> ComputationTree<R> tuple(ComputationTree<V0> t0, ComputationTree<V1> t1, ComputationTree<V2> t2, ComputationTree<V3> t3, ComputationTree<V4> t4, Tuple5Processor<V0, V1, V2, V3, V4, R> fn) {
        return new ComputationTree<R>(ImmutableList.of(t0, t1, t2, t3, t4), (input) -> {
              V0 v0 = getElementTyped(input, 0);
              V1 v1 = getElementTyped(input, 1);
              V2 v2 = getElementTyped(input, 2);
              V3 v3 = getElementTyped(input, 3);
              V4 v4 = getElementTyped(input, 4);
              return Either.left(fn.apply(v0, v1, v2, v3, v4));
        });
    }
}
