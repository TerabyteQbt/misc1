package misc1.commons.options;

import com.google.common.collect.ImmutableList;
import misc1.commons.ds.ImmutableSalvagingMap;

public final class ArgsView {
    private final ImmutableList<String> args;
    private final int start;
    private final int end;

    // keys are absolute
    private final ImmutableSalvagingMap<Integer, String> overrides;

    ArgsView(ImmutableList<String> args) {
        this.args = args;
        this.start = 0;
        this.end = args.size();
        this.overrides = ImmutableSalvagingMap.of();
    }

    private ArgsView(ImmutableList<String> args, int start, int end, ImmutableSalvagingMap<Integer, String> overrides) {
        this.args = args;
        this.start = start;
        this.end = end;
        this.overrides = overrides;
    }

    public String get(int iRelative) {
        int iAbsolute = start + iRelative;
        if(iAbsolute >= end) {
            throw new ArrayIndexOutOfBoundsException(iRelative);
        }
        String override = overrides.get(iAbsolute);
        if(override != null) {
            return override;
        }
        return args.get(iAbsolute);
    }

    public int size() {
        return end - start;
    }

    public ArgsView subList(int newStartRelative) {
        return subList(newStartRelative, size());
    }

    public ArgsView subList(int newStartRelative, int newEndRelative) {
        int newStartAbsolute = start + newStartRelative;
        int newEndAbsolute = start + newEndRelative;
        return new ArgsView(args, newStartAbsolute, newEndAbsolute, overrides);
    }

    public ArgsView override(int iRelative, String arg) {
        int iAbsolute = start + iRelative;
        return new ArgsView(args, start, end, overrides.simplePut(iAbsolute, arg));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i = 0; i < size(); ++i) {
            if(i > 0) {
                sb.append(", ");
            }
            sb.append(get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}
