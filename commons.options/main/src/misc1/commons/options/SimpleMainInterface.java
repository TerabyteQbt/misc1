package misc1.commons.options;

public interface SimpleMainInterface<O, E extends Throwable> {
    public int run(OptionsResults<O> options) throws E;
}
