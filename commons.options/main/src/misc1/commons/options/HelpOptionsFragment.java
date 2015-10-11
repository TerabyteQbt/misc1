package misc1.commons.options;

import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

public class HelpOptionsFragment<O> extends NamedOptionsFragment<O, ObjectUtils.Null, ObjectUtils.Null> {
    public HelpOptionsFragment(Iterable<String> matches, String helpDesc) {
        super(matches, null, helpDesc);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    protected boolean isOptional() {
        return true;
    }

    @Override
    public Pair<ObjectUtils.Null, Integer> match1(List<String> argsList) {
        throw new HelpRequestedException();
    }

    @Override
    public ObjectUtils.Null empty() {
        return ObjectUtils.NULL;
    }

    @Override
    public ObjectUtils.Null combine(ObjectUtils.Null lhs, ObjectUtils.Null rhs) {
        return ObjectUtils.NULL;
    }

    @Override
    public ObjectUtils.Null complete(ObjectUtils.Null intermediate) {
        return ObjectUtils.NULL;
    }
}
