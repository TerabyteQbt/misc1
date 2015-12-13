package misc1.commons.options;

import com.google.common.collect.ImmutableList;

public abstract class SimpleMain<O, E extends Throwable> implements SimpleMainInterface<O, E> {
    protected void exec(String[] args) throws E{
        execStatic(getOptionsClass(), this, args);
    }

    public static <O, E extends Throwable> void execStatic(Class<O> optionsClass, SimpleMainInterface<O, E> main, String[] args) throws E{
        int exit = runStatic(optionsClass, main, args);
        System.exit(exit);
    }

    protected int run(String[] args) throws E {
        return runStatic(getOptionsClass(), this, args);
    }

    public static <O, E extends Throwable> int runStatic(Class<O> optionsClass, SimpleMainInterface<O, E> main, String[] args) throws E{
        OptionsResults<O> options;
        try {
            options = OptionsResults.parse(optionsClass, args);
        }
        catch(HelpRequestedException e) {
            System.err.println("Options:");
            for(String line : OptionsResults.help(optionsClass)) {
                System.err.println("   " + line);
            }
            return 0;
        }
        catch(OptionsException e) {
            System.err.println(e.getMessage());
            return 1;
        }
        return main.run(options);
    }

    public static <O> HelpOptionsFragment<O> simpleHelpOption() {
        return new HelpOptionsFragment(ImmutableList.of("--help"), "Show help.");
    }

    protected abstract Class<O> getOptionsClass();
}
