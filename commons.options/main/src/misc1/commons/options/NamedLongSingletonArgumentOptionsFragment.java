package misc1.commons.options;

import misc1.commons.Maybe;

public class NamedLongSingletonArgumentOptionsFragment<O> extends NamedSingletonArgumentOptionsFragment<O, Long> {
    public NamedLongSingletonArgumentOptionsFragment(Iterable<String> matches, Maybe<Long> def, String helpDesc) {
        super(matches, def, helpDesc);
    }

    @Override
    protected Long map(String arg) {
        return Long.parseLong(arg, 10);
    }
}
