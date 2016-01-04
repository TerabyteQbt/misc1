package misc1.commons.tuple;

import com.google.common.base.Function;
import java.util.Comparator;
import org.apache.commons.lang3.tuple.Pair;

public final class Misc1PairUtils {
    private Misc1PairUtils() {
        // no
    }

    private static final Function<Pair<Object, Object>, Object> LEFT_FUNCTION = (pair) -> pair.getLeft();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <A, B> Function<Pair<A, B>, A> leftFunction() {
        return (Function)LEFT_FUNCTION;
    }

    private static final Function<Pair<Object, Object>, Object> RIGHT_FUNCTION = (pair) -> pair.getRight();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <A, B> Function<Pair<A, B>, B> rightFunction() {
        return (Function)RIGHT_FUNCTION;
    }

    public static <A, B> Comparator<Pair<A, B>> comparator(final Comparator<? super A> c1, final Comparator<? super B> c2) {
        return (p1, p2) -> {
            int r1 = c1.compare(p1.getLeft(), p2.getLeft());
            if(r1 != 0) {
                return r1;
            }
            return c2.compare(p1.getRight(), p2.getRight());
        };
    }
}
