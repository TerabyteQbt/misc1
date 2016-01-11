package misc1.commons.options;

public final class OptionsFragment<O, R> {
    final OptionsFragmentInternals<O, ?, R> delegate;

    OptionsFragment(OptionsFragmentInternals<O, ?, R> delegate) {
        this.delegate = delegate;
    }

    public OptionsFragment<O, R> helpDesc(String helpDesc) {
        return new OptionsFragment<O, R>(delegate.helpDesc(helpDesc));
    }

    public <R2> OptionsFragment<O, R2> transform(OptionsTransform<R, R2> f) {
        return new OptionsFragment<O, R2>(delegate.transform(f));
    }
}
