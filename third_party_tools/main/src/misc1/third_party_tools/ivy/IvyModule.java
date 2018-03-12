package misc1.third_party_tools.ivy;

import com.google.common.collect.ImmutableMap;
import misc1.commons.ds.ImmutableSalvagingMap;
import misc1.commons.ds.Struct;
import misc1.commons.ds.StructBuilder;
import misc1.commons.ds.StructKey;
import misc1.commons.ds.StructType;
import misc1.commons.ds.StructTypeBuilder;

public final class IvyModule extends Struct<IvyModule, IvyModule.Builder> {
    public final String group;
    public final String module;

    public static IvyModule of(String group, String module) {
        Builder b = TYPE.builder();
        b = b.set(GROUP, group);
        b = b.set(MODULE, module);
        return b.build();
    }

    private IvyModule(ImmutableMap<StructKey<IvyModule, ?, ?>, Object> map) {
        super(TYPE, map);

        this.group = get(GROUP);
        this.module = get(MODULE);
    }

    @Override
    public String toString() {
        return group + ":" + module;
    }

    public IvyModuleAndVersion withVersion(String version) {
        return IvyModuleAndVersion.of(group, module, version);
    }

    public static class Builder extends StructBuilder<IvyModule, Builder> {
        public Builder(ImmutableSalvagingMap<StructKey<IvyModule, ?, ?>, Object> map) {
            super(TYPE, map);
        }
    }

    public static final StructKey<IvyModule, String, String> GROUP;
    public static final StructKey<IvyModule, String, String> MODULE;
    public static final StructType<IvyModule, Builder> TYPE;
    static {
        StructTypeBuilder<IvyModule, Builder> b = new StructTypeBuilder<>(IvyModule::new, Builder::new);

        GROUP = b.<String>key("group").add();
        MODULE = b.<String>key("module").add();

        TYPE = b.build();
    }
}
