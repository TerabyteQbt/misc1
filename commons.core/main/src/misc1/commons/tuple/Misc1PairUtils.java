package misc1.commons.tuple;

import com.google.common.base.Function;
import org.apache.commons.lang3.tuple.Pair;

public final class Misc1PairUtils {
    private Misc1PairUtils() {
        // no
    }

    private static final Function<Pair<Object, Object>, Object> LEFT_FUNCTION = new Function<Pair<Object, Object>, Object>() {
        @Override
        public Object apply(Pair<Object, Object> pair) {
            return pair.getLeft();
        }
    };

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <A, B> Function<Pair<A, B>, A> leftFunction() {
        return (Function)LEFT_FUNCTION;
    }

    private static final Function<Pair<Object, Object>, Object> RIGHT_FUNCTION = new Function<Pair<Object, Object>, Object>() {
        @Override
        public Object apply(Pair<Object, Object> pair) {
            return pair.getRight();
        }
    };

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <A, B> Function<Pair<A, B>, B> rightFunction() {
        return (Function)RIGHT_FUNCTION;
    }
}
