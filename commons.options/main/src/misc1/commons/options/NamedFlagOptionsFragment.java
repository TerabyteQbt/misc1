package misc1.commons.options;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public abstract class NamedFlagOptionsFragment<O, R> extends NamedOptionsFragment<O, Integer, R> {
    public NamedFlagOptionsFragment(Iterable<String> matches, String helpDesc) {
        super(matches, null, helpDesc);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Pair<Integer, Integer> match1(List<String> argsList) {
        return Pair.of(1, 0);
    }

    @Override
    public Integer empty() {
        return 0;
    }

    @Override
    public Integer combine(Integer lhs, Integer rhs) {
        return lhs + rhs;
    }

    @Override
    public abstract R complete(Integer count);
}
