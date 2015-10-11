package misc1.commons.options;

public class OptionsFragmentStatus<M, R> {
    private final OptionsFragment<?, M, R> optionsFragment;
    private Delegate delegate = new EmptyDelegate();

    private abstract class Delegate {
        public abstract Delegate addIntermediate(M intermediate);
        public abstract Delegate addResult(R result);
        public abstract R complete();
    }

    private class EmptyDelegate extends Delegate {
        private Delegate emptyReplacement() {
            return new IntermediateDelegate(optionsFragment.empty());
        }

        @Override
        public Delegate addIntermediate(M intermediate) {
            return emptyReplacement().addIntermediate(intermediate);
        }

        @Override
        public Delegate addResult(R result) {
            return new ResultDelegate(result);
        }

        @Override
        public R complete() {
            return emptyReplacement().complete();
        }
    }

    private class IntermediateDelegate extends Delegate {
        private final M intermediate;

        public IntermediateDelegate(M intermediate) {
            this.intermediate = intermediate;
        }

        @Override
        public Delegate addIntermediate(M intermediate2) {
            return new IntermediateDelegate(optionsFragment.combine(intermediate, intermediate2));
        }

        @Override
        public Delegate addResult(R result) {
            throw new IllegalStateException();
        }

        @Override
        public R complete() {
            return optionsFragment.complete(intermediate);
        }
    }

    private class ResultDelegate extends Delegate {
        private final R result;

        public ResultDelegate(R result) {
            this.result = result;
        }

        @Override
        public Delegate addIntermediate(M intermediate) {
            throw new IllegalStateException();
        }

        @Override
        public Delegate addResult(R result2) {
            throw new IllegalStateException();
        }

        @Override
        public R complete() {
            return result;
        }
    }

    public OptionsFragmentStatus(OptionsFragment<?, M, R> optionsFragment) {
        this.optionsFragment = optionsFragment;
    }

    public void addIntermediate(M intermediate) {
        delegate = delegate.addIntermediate(intermediate);
    }

    public void addResult(R result) {
        delegate = delegate.addResult(result);
    }

    public R complete() {
        return delegate.complete();
    }
}
