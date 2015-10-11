package misc1.third_party_tools.ivy;

import com.google.common.base.Function;

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

    public static final Function<String, PatternIvyModuleAndVersion> PARSE = new Function<String, PatternIvyModuleAndVersion>() {
        @Override
        public PatternIvyModuleAndVersion apply(String arg) {
            return new PatternIvyModuleAndVersion(arg);
        }
    };
}
