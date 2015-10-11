package misc1.commons.options;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface OptionsFragment<O, M, R> {
    public int getPriority();
    public Pair<M, Integer> match(List<String> argsList);
    public M empty();
    public M combine(M lhs, M rhs);
    public R complete(M intermediate);
    public String getHelpKey();
    public String getHelpDesc();
}
