package misc1.third_party_tools.ivy;

public final class PatternIvyModuleAndVersion extends BaseIvyModuleAndVersion {
    public PatternIvyModuleAndVersion(String arg) {
        super(arg);
    }

    @Override
    protected boolean validateGroup(String group) {
        return true;
    }

    @Override
    protected boolean validateModule(String module) {
        return true;
    }

    @Override
    protected boolean validateVersion(String version) {
        return true;
    }

    public boolean matches(IvyModuleAndVersion mv) {
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
}
