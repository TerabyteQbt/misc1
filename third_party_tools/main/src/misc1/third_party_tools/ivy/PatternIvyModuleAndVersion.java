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
