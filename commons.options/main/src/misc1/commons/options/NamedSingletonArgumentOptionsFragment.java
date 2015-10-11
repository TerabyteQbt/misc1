package misc1.commons.options;

import com.google.common.collect.ImmutableList;
import misc1.commons.Maybe;

public abstract class NamedSingletonArgumentOptionsFragment<O, R> extends NamedArgumentOptionsFragment<O, R> {
    private final Maybe<R> def;

    public NamedSingletonArgumentOptionsFragment(Iterable<String> matches, Maybe<R> def, String helpDesc) {
        super(matches, helpDesc);
        this.def = def;
    }

    @Override
    protected boolean isOptional() {
        return def.isPresent();
    }

    @Override
    public R complete(ImmutableList<String> args) {
        if(args.size() == 0) {
            if(def.isPresent()) {
                return def.get(null);
            }
            throw new OptionsException(name() + " is required");
        }
        if(args.size() == 1) {
            return map(args.get(0));
        }
        throw new OptionsException(name() + " cannot be specified more than once");
    }

    protected abstract R map(String arg);
}
