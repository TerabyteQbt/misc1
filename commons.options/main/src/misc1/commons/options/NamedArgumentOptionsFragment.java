package misc1.commons.options;

import com.google.common.collect.ImmutableList;
import java.util.List;
import misc1.commons.ds.LazyCollector;
import org.apache.commons.lang3.tuple.Pair;

public abstract class NamedArgumentOptionsFragment<O, R> extends NamedOptionsFragment<O, LazyCollector<String>, R> {
    public NamedArgumentOptionsFragment(Iterable<String> matches, String helpDesc) {
        super(matches, "ARG", helpDesc);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Pair<LazyCollector<String>, Integer> match1(List<String> argsList) {
        if(argsList.size() == 0) {
            throw new OptionsException(name() + " requires an argument");
        }
        return Pair.of(LazyCollector.of(argsList.get(0)), 1);
    }

    @Override
    public LazyCollector<String> empty() {
        return LazyCollector.of();
    }

    @Override
    public LazyCollector<String> combine(LazyCollector<String> lhs, LazyCollector<String> rhs) {
        return lhs.union(rhs);
    }

    @Override
    public R complete(LazyCollector<String> intermediate) {
        return complete(intermediate.forceList());
    }

    @Override
    abstract protected boolean isOptional();
    abstract protected R complete(ImmutableList<String> args);
}
