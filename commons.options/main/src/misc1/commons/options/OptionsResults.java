package misc1.commons.options;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import misc1.commons.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class OptionsResults<O> {
    private final ImmutableMap<OptionsFragment<? super O, ?, ?>, Optional<Object>> map;

    private OptionsResults(ImmutableMap<OptionsFragment<? super O, ?, ?>, Optional<Object>> map) {
        this.map = map;
    }

    public <R> R get(OptionsFragment<? super O, ?, R> fragment) {
        @SuppressWarnings("unchecked")
        Optional<R> ret = (Optional<R>)map.get(fragment);
        return ret.orNull();
    }

    public static <O> OptionsResults<O> parse(Class<O> clazz, String... argsArray) {
        return parse(clazz, ImmutableList.copyOf(argsArray));
    }

    public static <O> OptionsResults<O> parse(Class<O> clazz, Iterable<String> argsIterable) {
        List<String> args = ImmutableList.copyOf(argsIterable);

        int index = 0;
        OptionsResults.Builder<O> b = OptionsResults.builder(clazz);
        while(index < args.size()) {
            List<String> subArgs = args.subList(index, args.size());
            Pair<OptionsFragment<? super O, ?, ?>, Pair<?, Integer>> match = null;
            for(OptionsFragment<? super O, ?, ?> optionsFragment : b.optionsFragments) {
                if(match != null && optionsFragment.getPriority() < match.getLeft().getPriority()) {
                    continue;
                }
                Pair<?, Integer> result = optionsFragment.match(subArgs);
                if(result == null) {
                    continue;
                }
                Pair<OptionsFragment<? super O, ?, ?>, Pair<?, Integer>> resultMatch = Pair.<OptionsFragment<? super O, ?, ?>, Pair<?, Integer>>of(optionsFragment, result);
                if(match != null && optionsFragment.getPriority() == match.getLeft().getPriority()) {
                    throw new IllegalStateException("Options match conflict: " + match + "/" + resultMatch);
                }
                match = resultMatch;
            }
            if(match == null) {
                throw new OptionsException("No match for arguments: " + subArgs);
            }
            b = b.addIntermediateUnchecked(match.getLeft(), match.getRight().getLeft());
            index += match.getRight().getRight();
        }

        return b.build();
    }

    public static <O> List<String> help(Class<O> clazz) {
        ArrayList<OptionsFragment<? super O, ?, ?>> optionsFragments = Lists.newArrayList(getFragmentsFromInterface(clazz));
        Collections.sort(optionsFragments, (o1, o2) -> {
            if(o1.getPriority() < o2.getPriority()) {
                return 1;
            }
            if(o1.getPriority() > o2.getPriority()) {
                return -1;
            }
            return o1.getHelpKey().compareTo(o2.getHelpKey());
        });
        ImmutableList.Builder<String> b = ImmutableList.builder();
        for(OptionsFragment<? super O, ?, ?> optionsFragment : optionsFragments) {
            b.add(optionsFragment.getHelpDesc());
        }
        return b.build();
    }

    public static class Builder<O> {
        final ImmutableList<OptionsFragment<? super O, ?, ?>> optionsFragments;
        private final Map<OptionsFragment<? super O, ?, ?>, OptionsFragmentStatus<?, ?>> statuses = Maps.newHashMap();

        private Builder(Class<O> clazz) {
            this.optionsFragments = getFragmentsFromInterface(clazz);
            for(OptionsFragment<? super O, ?, ?> optionsFragment : optionsFragments) {
                addStatus(optionsFragment);
            }
        }

        @SuppressWarnings("unchecked")
        private Builder<O> addIntermediateUnchecked(OptionsFragment<? super O, ?, ?> optionsFragment, Object intermediate) {
            return addIntermediate((OptionsFragment<? super O, Object, ?>)optionsFragment, intermediate);
        }

        private <M, R> void addStatus(OptionsFragment<? super O, M, R> optionsFragment) {
            statuses.put(optionsFragment, new OptionsFragmentStatus<M, R>(optionsFragment));
        }

        @SuppressWarnings("unchecked")
        private <M, R> OptionsFragmentStatus<M, R> getStatus(OptionsFragment<? super O, M, R> optionsFragment) {
            return (OptionsFragmentStatus<M, R>)statuses.get(optionsFragment);
        }

        public <M> Builder<O> addIntermediate(OptionsFragment<? super O, M, ?> optionsFragment, M intermediate) {
            getStatus(optionsFragment).addIntermediate(intermediate);
            return this;
        }

        public <R> Builder<O> addResult(OptionsFragment<? super O, ?, R> optionsFragment, R result) {
            getStatus(optionsFragment).addResult(result);
            return this;
        }

        public OptionsResults<O> build() {
            ImmutableMap.Builder<OptionsFragment<? super O, ?, ?>, Optional<Object>> b = ImmutableMap.builder();

            for(Map.Entry<OptionsFragment<? super O, ?, ?>, OptionsFragmentStatus<?, ?>> e : statuses.entrySet()) {
                b.put(e.getKey(), Optional.<Object>fromNullable(e.getValue().complete()));
            }

            return new OptionsResults<O>(b.build());
        }
    }

    public static <O> Builder<O> builder(Class<O> clazz) {
        return new Builder<O>(clazz);
    }

    private static <O> ImmutableList<OptionsFragment<? super O, ?, ?>> getFragmentsFromInterface(Class<O> clazz) {
        ImmutableList.Builder<OptionsFragment<? super O, ?, ?>> b = ImmutableList.builder();
        getFragmentsFromInterface(b, clazz);
        return b.build();
    }

    private static <O> void getFragmentsFromInterface(ImmutableList.Builder<OptionsFragment<? super O, ?, ?>> b, Class<O> clazz) {
        Set<Class<?>> checked = Sets.newHashSet();
        Deque<Class<?>> queue = Lists.newLinkedList();
        queue.add(clazz);
        while(!queue.isEmpty()) {
            Class<?> clazz2 = queue.pop();
            if(!checked.add(clazz2)) {
                continue;
            }
            if(clazz2 == null) {
                continue;
            }
            for(Field f : clazz2.getDeclaredFields()) {
                if(!Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                if(!Modifier.isPublic(f.getModifiers())) {
                    continue;
                }
                Object o;
                try {
                    o = f.get(null);
                }
                catch(Exception e) {
                    throw ExceptionUtils.commute(e);
                }
                getFragmentsFromObject(b, o);
            }
            queue.add(clazz2.getSuperclass());
            for(Class<?> clazz3 : clazz2.getInterfaces()) {
                queue.add(clazz3);
            }
        }
    }

    private static <O> void getFragmentsFromObject(ImmutableList.Builder<OptionsFragment<? super O, ?, ?>> b, Object o) {
        if(o instanceof OptionsFragment) {
            @SuppressWarnings("unchecked")
            OptionsFragment<? super O, ?, ?> o2 = (OptionsFragment<? super O, ?, ?>)o;
            b.add(o2);
        }
        if(o instanceof OptionsDelegate) {
            @SuppressWarnings("unchecked")
            OptionsDelegate<? super O> o2 = (OptionsDelegate<? super O>)o;
            getFragmentsFromDelegate(b, o2);
        }
    }

    private static <O> void getFragmentsFromDelegate(ImmutableList.Builder<OptionsFragment<? super O, ?, ?>> b, OptionsDelegate<? super O> od) {
        for(Class<?> clazz = od.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            for(Field f : clazz.getDeclaredFields()) {
                if(Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                if(!Modifier.isPublic(f.getModifiers())) {
                    continue;
                }
                Object o;
                try {
                    o = f.get(od);
                }
                catch(Exception e) {
                    throw ExceptionUtils.commute(e);
                }
                getFragmentsFromObject(b, o);
            }
        }
    }

    public static <O> OptionsResults<O> simpleParse(Class<O> clazz, String name, String... args) {
        return simpleParse(clazz, name, Arrays.asList(args));
    }

    public static <O> OptionsResults<O> simpleParse(Class<O> clazz, String name, Iterable<String> args) {
        try {
            return parse(clazz, args);
        }
        catch(HelpRequestedException e) {
            System.err.println("Usage: " + name + " ARGS");
            for(String line : help(clazz)) {
                System.err.println("   " + line);
            }
        }
        catch(OptionsException e) {
            System.err.println(e.getMessage());
        }
        System.exit(1);
        throw new IllegalStateException();
    }
}
