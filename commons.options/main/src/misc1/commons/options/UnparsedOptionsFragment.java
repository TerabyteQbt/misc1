package misc1.commons.options;

import com.google.common.collect.ImmutableList;
import java.util.List;
import misc1.commons.ds.LazyCollector;
import org.apache.commons.lang3.tuple.Pair;

public class UnparsedOptionsFragment<O> implements OptionsFragment<O, LazyCollector<String>, ImmutableList<String>> {
    private final String helpDesc;
    private final boolean hard;
    private final Integer min;
    private final Integer max;

    public UnparsedOptionsFragment(String helpDesc, boolean hard, Integer min, Integer max) {
        this.helpDesc = helpDesc;
        this.hard = hard;
        this.min = min;
        this.max = max;
    }

    @Override
    public int getPriority() {
        return -1;
    }

    @Override
    public Pair<LazyCollector<String>, Integer> match(List<String> argsList) {
        ImmutableList.Builder<String> kept = ImmutableList.builder();
        int i = 0;
        while(true) {
            if(i == argsList.size()) {
                break;
            }
            String next = argsList.get(i++);
            if(next.equals("--")) {
                // slurpTail or not we slurp it all
                kept.addAll(argsList.subList(i, argsList.size()));
                i = argsList.size();
                break;
            }
            kept.add(next);
            if(hard) {
                // hard: once we've fallen out of other parsing we don't stop
                continue;
            }
            else {
                // soft: return control to other parsing
                break;
            }
        }
        return Pair.of(LazyCollector.<String>of(kept.build()), i);
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
    public ImmutableList<String> complete(LazyCollector<String> intermediate) {
        ImmutableList<String> ret = intermediate.forceList();
        if(min != null && ret.size() < min) {
            throw new OptionsException("At least " + min + " extra arguments required");
        }
        if(max != null && ret.size() > max) {
            throw new OptionsException("At most " + max + " extra arguments allowed");
        }
        return ret;
    }

    @Override
    public String getHelpKey() {
        return "";
    }

    @Override
    public String getHelpDesc() {
        boolean optional = !(min != null && min > 0);
        return (optional ? "[ARGS]" : "ARGS") + " : " + helpDesc;
    }
}
