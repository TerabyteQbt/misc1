package misc1.third_party_tools.ivy;

import com.google.common.collect.ImmutableMap;
import misc1.commons.ds.ImmutableSalvagingMap;
import misc1.commons.ds.Struct;
import misc1.commons.ds.StructBuilder;
import misc1.commons.ds.StructKey;
import misc1.commons.ds.StructType;
import misc1.commons.ds.StructTypeBuilder;

public final class IvyModuleAndVersion extends Struct<IvyModuleAndVersion, IvyModuleAndVersion.Builder> {
    public final String group;
    public final String module;
    public final String version;

    public static IvyModuleAndVersion parse(String arg) {
        String[] parts = arg.split(":");
        if(parts.length != 3) {
            throw new IllegalArgumentException("Module must have exactly three parts: " + arg);
        }
        return of(parts[0], parts[1], parts[2]);
    }

    public static IvyModuleAndVersion of(String group, String module, String version) {
        Builder b = TYPE.builder();
        b = b.set(GROUP, group);
        b = b.set(MODULE, module);
        b = b.set(VERSION, version);
        return b.build();
    }

    private IvyModuleAndVersion(ImmutableMap<StructKey<IvyModuleAndVersion, ?, ?>, Object> map) {
        super(TYPE, map);

        this.group = get(GROUP);
        this.module = get(MODULE);
        this.version = get(VERSION);
    }

    @Override
    public String toString() {
        return group + ":" + module + ":" + version;
    }

    public boolean contains(IvyModuleAndVersion mv) {
        if(!check(group, mv.group)) {
            return false;
        }
        if(!check(module, mv.module)) {
            return false;
        }
        if(!check(version, mv.version)) {
            return false;
        }
        return true;
    }

    private static boolean check(String pattern, String value) {
        if(pattern.equals("*")) {
            return true;
        }
        if(pattern.equals(value)) {
            return true;
        }
        return false;
    }

    public IvyModule withoutVersion() {
        return IvyModule.of(group, module);
    }

    public static class Builder extends StructBuilder<IvyModuleAndVersion, Builder> {
        public Builder(ImmutableSalvagingMap<StructKey<IvyModuleAndVersion, ?, ?>, Object> map) {
            super(TYPE, map);
        }
    }

    public static final StructKey<IvyModuleAndVersion, String, String> GROUP;
    public static final StructKey<IvyModuleAndVersion, String, String> MODULE;
    public static final StructKey<IvyModuleAndVersion, String, String> VERSION;
    public static final StructType<IvyModuleAndVersion, Builder> TYPE;
    static {
        StructTypeBuilder<IvyModuleAndVersion, Builder> b = new StructTypeBuilder<>(IvyModuleAndVersion::new, Builder::new);

        GROUP = b.<String>key("group").add();
        MODULE = b.<String>key("module").add();
        VERSION = b.<String>key("version").add();

        TYPE = b.build();
    }
}
