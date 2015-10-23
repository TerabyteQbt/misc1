package misc1.commons.options;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
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
        String arg0 = argsList.get(0);
        for(String match : matches) {
            if(arg0.equals(match)) {
                Pair<M, Integer> matchResult = match1(argsList.subList(1, argsList.size()));
                if(matchResult != null) {
                    return Pair.of(matchResult.getLeft(), matchResult.getRight() + 1);
                }
            }

            if(match.startsWith("--") && arg0.startsWith(match + "=")) {
                ImmutableList.Builder<String> argsReplaced = ImmutableList.builder();
                argsReplaced.add(arg0.substring(match.length() + 1));
                argsReplaced.addAll(argsList.subList(1, argsList.size()));
                Pair<M, Integer> matchResult = match1(argsReplaced.build());
                if(matchResult != null && matchResult.getRight() >= 1) {
                    return Pair.of(matchResult.getLeft(), matchResult.getRight());
                }
            }

            if(match.startsWith("-") && match.length() == 2 && arg0.startsWith(match)) {
                ImmutableList.Builder<String> argsReplaced = ImmutableList.builder();
                argsReplaced.add(arg0.substring(match.length()));
                argsReplaced.addAll(argsList.subList(1, argsList.size()));
                Pair<M, Integer> matchResult = match1(argsReplaced.build());
                if(matchResult != null && matchResult.getRight() >= 1) {
                    return Pair.of(matchResult.getLeft(), matchResult.getRight());
                }
            }
        }
        return null;
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
