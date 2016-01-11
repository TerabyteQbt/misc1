package misc1.commons.options;

import com.google.common.collect.ImmutableList;

class OptionsFragmentInternals<O, M, R> {
    final OptionsMatcher<M> matcher;
    final OptionsTransform<ImmutableList<M>, R> process;
    final String helpDesc;

    OptionsFragmentInternals(OptionsMatcher<M> matcher, OptionsTransform<ImmutableList<M>, R> process, String helpDesc) {
        this.matcher = matcher;
        this.process = process;
        this.helpDesc = helpDesc;
    }

    OptionsFragmentInternals<O, M, R> helpDesc(String newHelpDesc) {
        return new OptionsFragmentInternals<O, M, R>(matcher, process, newHelpDesc);
    }

    <R2> OptionsFragmentInternals<O, M, R2> transform(OptionsTransform<R, R2> newProcess) {
        OptionsTransform<ImmutableList<M>, R2> composedProcess = (helpDesc, input) -> {
            R intermediate = process.apply(helpDesc, input);
            return newProcess.apply(helpDesc, intermediate);
        };
        return new OptionsFragmentInternals<O, M, R2>(matcher, composedProcess, helpDesc);
    }

    int getPriority() {
        return matcher.getPriority();
    }

    String getHelpKey() {
        return matcher.getHelpKey();
    }

    String getHelpDesc() {
        String ret = matcher.getHelpDesc();
        if(helpDesc != null) {
            ret += " : " + helpDesc;
        }
        return ret;
    }
}
