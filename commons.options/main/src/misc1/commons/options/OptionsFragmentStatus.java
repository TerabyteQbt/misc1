package misc1.commons.options;

import misc1.commons.ds.LazyCollector;

public class OptionsFragmentStatus<M, R> {
    private final OptionsFragmentInternals<?, M, R> optionsFragment;
    private Delegate delegate = new EmptyDelegate();

    private abstract class Delegate {
        public abstract Delegate addIntermediate(M intermediate);
        public abstract Delegate addResult(R result);
        public abstract R complete();
    }

    private class EmptyDelegate extends Delegate {
        private Delegate emptyReplacement() {
            return new IntermediateDelegate(LazyCollector.of());
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
        private final LazyCollector<M> list;

        public IntermediateDelegate(LazyCollector<M> list) {
            this.list = list;
        }

        @Override
        public Delegate addIntermediate(M intermediate) {
            return new IntermediateDelegate(list.union(LazyCollector.of(intermediate)));
        }

        @Override
        public Delegate addResult(R result) {
            throw new IllegalStateException();
        }

        @Override
        public R complete() {
            return optionsFragment.process.apply(optionsFragment.matcher.getHelpDesc(), list.forceList());
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

    public OptionsFragmentStatus(OptionsFragmentInternals<?, M, R> optionsFragment) {
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
