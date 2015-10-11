package misc1.third_party_tools.ivy;

import com.google.common.base.Function;

public final class IvyModuleAndVersion extends BaseIvyModuleAndVersion {
    public IvyModuleAndVersion(String arg) {
        super(arg);
    }

    public IvyModuleAndVersion(String module, String group, String version) {
        super(module, group, version);
    }

    @Override
    protected boolean validateGroup(String group) {
        return !group.equals("*");
    }

    @Override
    protected boolean validateModule(String module) {
        return !module.equals("*");
    }

    @Override
    protected boolean validateVersion(String version) {
        return !version.equals("*");
    }

    public static final Function<String, IvyModuleAndVersion> PARSE = new Function<String, IvyModuleAndVersion>() {
        @Override
        public IvyModuleAndVersion apply(String arg) {
            return new IvyModuleAndVersion(arg);
        }
    };
}
