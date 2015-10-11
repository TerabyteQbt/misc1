package misc1.commons.options;

import misc1.commons.Maybe;

public class NamedIntegerSingletonArgumentOptionsFragment<O> extends NamedSingletonArgumentOptionsFragment<O, Integer> {
    public NamedIntegerSingletonArgumentOptionsFragment(Iterable<String> matches, Maybe<Integer> def, String helpDesc) {
        super(matches, def, helpDesc);
    }

    @Override
    protected Integer map(String arg) {
        try {
            return Integer.parseInt(arg, 10);
        }
        catch(NumberFormatException e) {
            throw new OptionsException(name() + " requires an integer: " + arg);
        }
    }
}
