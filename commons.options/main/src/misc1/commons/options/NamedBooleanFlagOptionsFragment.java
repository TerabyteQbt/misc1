package misc1.commons.options;

public class NamedBooleanFlagOptionsFragment<O> extends NamedFlagOptionsFragment<O, Boolean> {
    public NamedBooleanFlagOptionsFragment(Iterable<String> matches, String helpDesc) {
        super(matches, helpDesc);
    }

    @Override
    protected boolean isOptional() {
        return true;
    }

    @Override
    public Boolean complete(Integer count) {
        if(count == 0) {
            return false;
        }
        if(count == 1) {
            return true;
        }
        throw new OptionsException(name() + " cannot be specified more than once");
    }
}
