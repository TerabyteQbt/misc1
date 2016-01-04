package misc1.third_party_tools.ivy;

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
}
