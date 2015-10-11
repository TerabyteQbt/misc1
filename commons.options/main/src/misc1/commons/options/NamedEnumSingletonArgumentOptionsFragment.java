package misc1.commons.options;

import misc1.commons.Maybe;

public class NamedEnumSingletonArgumentOptionsFragment<O, E extends Enum<E>> extends NamedSingletonArgumentOptionsFragment<O, E> {
    private final Class<E> clazz;

    public NamedEnumSingletonArgumentOptionsFragment(Class<E> clazz, Iterable<String> matches, Maybe<E> def, String helpDesc) {
        super(matches, def, helpDesc);

        this.clazz = clazz;
    }

    @Override
    protected E map(String arg) {
        return Enum.valueOf(clazz, arg);
    }
}
