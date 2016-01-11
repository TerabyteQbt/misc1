package misc1.commons.options;

import org.apache.commons.lang3.tuple.Pair;

public interface OptionsMatcher<M> {
    int getPriority();
    Pair<M, ArgsView> match(ArgsView args);
    String getHelpKey();
    String getHelpDesc();
}
