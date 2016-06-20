//   Copyright 2016 Keith Amling
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package misc1.third_party_tools.ivy;

import com.google.common.base.Objects;

public abstract class BaseIvyModuleAndVersion {
    public final String group;
    public final String module;
    public final String version;

    BaseIvyModuleAndVersion(String arg) {
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

    private BaseIvyModuleAndVersion(String[] parts) {
        this(parts[0], parts[1], parts[2]);
    }

    BaseIvyModuleAndVersion(String group, String module, String version) {
        this.group = group;
        this.module = module;
        this.version = version;
        if(!validateGroup(group)) {
            throw new IllegalArgumentException("Invalid group for " + getClass().getSimpleName() + ": " + group);
        }
        if(!validateModule(module)) {
            throw new IllegalArgumentException("Invalid module for " + getClass().getSimpleName() + ": " + module);
        }
        if(!validateVersion(version)) {
            throw new IllegalArgumentException("Invalid version for " + getClass().getSimpleName() + ": " + version);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(group, module, version);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BaseIvyModuleAndVersion)) {
            return false;
        }
        if(obj.getClass() != this.getClass()) {
            return false;
        }
        BaseIvyModuleAndVersion other = (BaseIvyModuleAndVersion)obj;
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

    protected abstract boolean validateGroup(String group);
    protected abstract boolean validateModule(String module);
    protected abstract boolean validateVersion(String version);
}
