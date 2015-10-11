package misc1.commons.options;

import com.google.common.collect.ImmutableList;

public class NamedStringListArgumentOptionsFragment<O> extends NamedArgumentOptionsFragment<O, ImmutableList<String>> {
    private final Integer min;
    private final Integer max;

    public NamedStringListArgumentOptionsFragment(Iterable<String> matches, String helpDesc) {
        this(matches, null, null, helpDesc);
    }

    public NamedStringListArgumentOptionsFragment(Iterable<String> matches, Integer min, Integer max, String helpDesc) {
        super(matches, helpDesc);
        this.min = min;
        this.max = max;
    }

    @Override
    protected boolean isOptional() {
        return !(min != null && min > 0);
    }

    @Override
    protected ImmutableList<String> complete(ImmutableList<String> args) {
        if(min != null && args.size() < min) {
            throw new OptionsException(name() + " requires at least " + min + " values");
        }
        if(max != null && args.size() > max) {
            throw new OptionsException(name() + " allows at most " + max + " values");
        }
        return args;
    }
}
