package misc1.commons.options;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import misc1.commons.Maybe;
import misc1.commons.ds.LazyCollector;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

public final class OptionsLibrary<O> {
    private OptionsLibrary() {
    }

    public static <O> OptionsLibrary<O> of() {
        return new OptionsLibrary<O>();
    }

    private static void buildHelpDesc(StringBuilder sb, String... names) {
        boolean first = true;
        for(String name : names) {
            if(first) {
                first = false;
            }
            else {
                sb.append("|");
            }
            if(name.length() == 1) {
                sb.append("-");
                sb.append(name);
            }
            else {
                sb.append("--");
                sb.append(name);
            }
        }
    }

    public OptionsFragment<O, Integer> zeroArg(final String... names) {
        OptionsMatcher<ObjectUtils.Null> matcher = new OptionsMatcher<ObjectUtils.Null>() {
            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public Pair<ObjectUtils.Null, ArgsView> match(ArgsView args) {
                String arg0 = args.get(0);
                for(String name : names) {
                    if(name.length() == 1) {
                        if(arg0.equals("-" + name)) {
                            return Pair.of(ObjectUtils.NULL, args.subList(1));
                        }
                        if(arg0.startsWith("-" + name)) {
                            return Pair.of(ObjectUtils.NULL, args.override(0, "-" + arg0.substring(2)));
                        }
                    }
                    else {
                        if(arg0.equals("--" + name)) {
                            return Pair.of(ObjectUtils.NULL, args.subList(1));
                        }
                    }
                }
                return null;
            }

            @Override
            public String getHelpKey() {
                return names[0];
            }

            @Override
            public String getHelpDesc() {
                StringBuilder sb = new StringBuilder();
                buildHelpDesc(sb, names);
                return sb.toString();
            }
        };
        return of(matcher).transform((helpDesc, list) -> list.size());
    }

    public OptionsFragment<O, ImmutableList<String>> oneArg(String... names) {
        OptionsMatcher<String> matcher = new OptionsMatcher<String>() {
            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public Pair<String, ArgsView> match(ArgsView args) {
                String arg0 = args.get(0);
                for(String name : names) {
                    if(name.length() == 1) {
                        if(arg0.equals("-" + name) && args.size() >= 2) {
                            return Pair.of(args.get(1), args.subList(2));
                        }
                        if(arg0.startsWith("-" + name) && arg0.length() >= 3) {
                            return Pair.of(arg0.substring(2), args.subList(1));
                        }
                    }
                    else {
                        if(arg0.equals("--" + name) && args.size() >= 2) {
                            return Pair.of(args.get(1), args.subList(2));
                        }
                        String prefix = "--" + name + "=";
                        if(arg0.startsWith(prefix)) {
                            return Pair.of(arg0.substring(prefix.length()), args.subList(1));
                        }
                    }
                }
                return null;
            }

            @Override
            public String getHelpKey() {
                return names[0];
            }

            @Override
            public String getHelpDesc() {
                StringBuilder sb = new StringBuilder();
                buildHelpDesc(sb, names);
                sb.append(" <arg>");
                return sb.toString();
            }
        };
        return of(matcher);
    }

    public OptionsFragment<O, ImmutableList<Pair<String, String>>> twoArg(String... names) {
        OptionsMatcher<Pair<String, String>> matcher = new OptionsMatcher<Pair<String, String>>() {
            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public Pair<Pair<String, String>, ArgsView> match(ArgsView args) {
                String arg0 = args.get(0);
                for(String name : names) {
                    if(name.length() == 1) {
                        if(arg0.equals("-" + name) && args.size() >= 3) {
                            return Pair.of(Pair.of(args.get(1), args.get(2)), args.subList(3));
                        }
                    }
                    else {
                        if(arg0.equals("--" + name) && args.size() >= 3) {
                            return Pair.of(Pair.of(args.get(1), args.get(2)), args.subList(3));
                        }
                    }
                }
                return null;
            }

            @Override
            public String getHelpKey() {
                return names[0];
            }

            @Override
            public String getHelpDesc() {
                StringBuilder sb = new StringBuilder();
                buildHelpDesc(sb, names);
                sb.append(" <arg>");
                return sb.toString();
            }
        };
        return of(matcher);
    }

    public OptionsFragment<O, Maybe<Boolean>> trinary(String... names) {
        return oneArg(names).transform((helpDesc, list) -> {
            if(list.isEmpty()) {
                return Maybe.not();
            }
            if(list.size() > 1) {
                throw new OptionsException("Must be specified exactly zero or one times: " + helpDesc);
            }
            String val = Iterables.getOnlyElement(list);
            if(val.equalsIgnoreCase("true")) {
                return Maybe.of(true);
            }
            if(val.equalsIgnoreCase("false")) {
                return Maybe.of(false);
            }
            if(val.equalsIgnoreCase("unset")) {
                return Maybe.not();
            }
            throw new OptionsException("Must be one of 'true', 'false', or 'unset': " + helpDesc);
        });
    }

    public <M> OptionsFragment<O, ImmutableList<M>> of(OptionsMatcher<M> matcher) {
        OptionsFragmentInternals<O, M, ImmutableList<M>> delegate = new OptionsFragmentInternals<O, M, ImmutableList<M>>(matcher, (helpDesc, input) -> input, null);
        return new OptionsFragment<O, ImmutableList<M>>(delegate);
    }

    public OptionsFragment<O, ImmutableList<String>> unparsed(boolean hard) {
        OptionsMatcher<LazyCollector<String>> matcher = new OptionsMatcher<LazyCollector<String>>() {
            @Override
            public int getPriority() {
                return -1;
            }

            @Override
            public Pair<LazyCollector<String>, ArgsView> match(ArgsView args) {
                LazyCollector<String> kept = LazyCollector.of();
                while(true) {
                    if(args.size() == 0) {
                        break;
                    }
                    String next = args.get(0);
                    args = args.subList(1);
                    if(next.equals("--")) {
                        // hard or not we slurp it all
                        for(int i = 0; i < args.size(); ++i) {
                            kept = kept.union(LazyCollector.of(args.get(i)));
                        }
                        args = args.subList(args.size());
                        break;
                    }
                    kept = kept.union(LazyCollector.of(next));
                    if(hard) {
                        // hard: once we've fallen out of other parsing we don't stop
                        continue;
                    }
                    else {
                        // soft: return control to other parsing
                        break;
                    }
                }
                return Pair.of(kept, args);
            }

            @Override
            public String getHelpKey() {
                return "";
            }

            @Override
            public String getHelpDesc() {
                return "<extra arg(s)>";
            }
        };
        return of(matcher).transform((helpDesc, intermediates) -> {
            LazyCollector<String> all = LazyCollector.of();
            for(LazyCollector<String> intermediate : intermediates) {
                all = all.union(intermediate);
            }
            return all.forceList();
        });
    }

    public OptionsTransform<Integer, ObjectUtils.Null> help() {
        return (helpDesc, n) -> {
            if(n > 0) {
                throw new HelpRequestedException();
            }
            return ObjectUtils.NULL;
        };
    }

    public <T> OptionsTransform<ImmutableList<T>, ImmutableList<T>> min(int min) {
        return minMax(min, null);
    }

    public <T> OptionsTransform<ImmutableList<T>, ImmutableList<T>> max(int max) {
        return minMax(null, max);
    }

    public <T> OptionsTransform<ImmutableList<T>, ImmutableList<T>> minMax(Integer min, Integer max) {
        return (helpDesc, list) -> {
            if(min != null && list.size() < min) {
                throw new OptionsException("Must be specified at least " + min + " times: " + helpDesc);
            }
            if(max != null && list.size() > max) {
                throw new OptionsException("Must be specified no more than " + max + " times: " + helpDesc);
            }
            return list;
        };
    }

    public <T> OptionsTransform<ImmutableList<T>, T> singleton() {
        return singletonMaybe(Maybe.not());
    }

    public <T> OptionsTransform<ImmutableList<T>, T> singleton(T def) {
        return singletonMaybe(Maybe.of(def));
    }

    public <T> OptionsTransform<ImmutableList<T>, T> singletonMaybe(Maybe<T> def) {
        return (helpDesc, list) -> {
            if(list.size() == 0) {
                if(def.isPresent()) {
                    return def.get(null);
                }
                throw new OptionsException("Must be specified: " + helpDesc);
            }
            if(list.size() == 1) {
                return list.get(0);
            }
            throw new OptionsException("Must be specified no more than once: " + helpDesc);
        };
    }

    public OptionsTransform<Integer, Boolean> flag() {
        return (helpDesc, n) -> {
            switch(n) {
                case 0:
                    return false;

                case 1:
                    return true;

                default:
                    throw new OptionsException("Must be specified exactly zero or one times: " + helpDesc);
            }
        };
    }

    public OptionsTransform<String, Integer> parseInt() {
        return (helpDesc, s) -> {
            if(s == null) {
                return null;
            }
            return Integer.parseInt(s, 10);
        };
    }

    public <E extends Enum<E>> OptionsTransform<String, E> parseEnum(Class<E> clazz) {
        return (helpDesc, s) -> {
            if(s == null) {
                return null;
            }
            return Enum.valueOf(clazz, s);
        };
    }
}
