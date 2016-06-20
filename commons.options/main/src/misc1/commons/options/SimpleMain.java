//   Copyright 2016 Keith Amling
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package misc1.commons.options;

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

    public static <O> OptionsFragment<O, ?> simpleHelpOption() {
        OptionsLibrary<O> o = OptionsLibrary.of();
        return o.zeroArg("help").transform(o.help()).helpDesc("Show help.");
    }

    protected abstract Class<O> getOptionsClass();
}
