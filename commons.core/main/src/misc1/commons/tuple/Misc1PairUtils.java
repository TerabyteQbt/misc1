package misc1.commons.tuple;

import java.util.Comparator;
import org.apache.commons.lang3.tuple.Pair;

public final class Misc1PairUtils {
    private Misc1PairUtils() {
        // no
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
