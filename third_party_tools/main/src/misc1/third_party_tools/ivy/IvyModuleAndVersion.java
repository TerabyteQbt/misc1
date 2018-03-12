package misc1.third_party_tools.ivy;

import com.google.common.base.Objects;

public final class IvyModuleAndVersion {
    public final String group;
    public final String module;
    public final String version;

    public IvyModuleAndVersion(String arg) {
        this(checkedSplit(arg));
    }

    // getting around dumb-ass java insistence that this() be first statement in ctor
    private static String[] checkedSplit(String arg) {
        String[] parts = arg.split(":");
        if(parts.length != 3) {
            throw new IllegalArgumentException("Module must have exactly three parts: " + arg);
        }
        return parts;
    }

    private IvyModuleAndVersion(String[] parts) {
        this(parts[0], parts[1], parts[2]);
    }

    public IvyModuleAndVersion(String group, String module, String version) {
        this.group = group;
        this.module = module;
        this.version = version;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(group, module, version);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof IvyModuleAndVersion)) {
            return false;
        }
        if(obj.getClass() != this.getClass()) {
            return false;
        }
        IvyModuleAndVersion other = (IvyModuleAndVersion)obj;
        if(!group.equals(other.group)) {
            return false;
        }
        if(!module.equals(other.module)) {
            return false;
        }
        if(!version.equals(other.version)) {
            return false;
        }
        return true;
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
}
