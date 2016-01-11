package misc1.commons.options;

public interface OptionsTransform<A, B> {
    public B apply(String helpDesc, A input);
}
