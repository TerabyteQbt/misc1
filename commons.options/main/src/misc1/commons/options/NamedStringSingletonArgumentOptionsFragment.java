package misc1.commons.options;

import misc1.commons.Maybe;

public class NamedStringSingletonArgumentOptionsFragment<O> extends NamedSingletonArgumentOptionsFragment<O, String> {
    public NamedStringSingletonArgumentOptionsFragment(Iterable<String> matches, Maybe<String> def, String helpDesc) {
        super(matches, def, helpDesc);
    }

    @Override
    protected String map(String arg) {
        return arg;
    }
}
