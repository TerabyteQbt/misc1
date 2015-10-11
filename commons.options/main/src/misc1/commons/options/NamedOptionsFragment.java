package misc1.commons.options;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public abstract class NamedOptionsFragment<O, M, R> implements OptionsFragment<O, M, R> {
    private final ImmutableSet<String> matches;
    private final String helpArgs;
    private final String helpDesc;

    public NamedOptionsFragment(Iterable<String> matches, String helpArgs, String helpDesc) {
        this.matches = ImmutableSet.copyOf(matches);
        this.helpArgs = helpArgs;
        this.helpDesc = helpDesc;
    }

    @Override
    public final Pair<M, Integer> match(List<String> argsList) {
        if(!matches.contains(argsList.get(0))) {
            return null;
        }
        Pair<M, Integer> match = match1(argsList.subList(1, argsList.size()));
        if(match == null) {
            return null;
        }
        return Pair.of(match.getLeft(), match.getRight() + 1);
    }

    @Override
    public String getHelpKey() {
        String ret = name();
        while(ret.charAt(0) == '-') {
            ret = ret.substring(1);
        }
        return ret;
    }

    @Override
    public String getHelpDesc() {
        String leftText = Joiner.on("|").join(matches) + (helpArgs == null ? "" : (" " + helpArgs));
        if(isOptional()) {
            leftText = "[" + leftText + "]";
        }
        return leftText + " : " + helpDesc;
    }

    protected String name() {
        return Iterables.getFirst(matches, "<unknown>");
    }

    protected abstract boolean isOptional();
    protected abstract Pair<M, Integer> match1(List<String> argsList);
}
