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
package misc1.commons.ds;

import com.google.common.base.Optional;
import misc1.commons.merge.Merge;
import misc1.commons.merge.Merges;

public abstract class StructKey<S, VS, VB> {
    public final String name;
    private final Optional<VB> def;

    public StructKey(String name) {
        this.name = name;
        this.def = Optional.absent();
    }

    public StructKey(String name, VB vb) {
        this.name = name;
        this.def = Optional.of(vb);
    }

    public Optional<VB> getDefault() {
        return def;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract VS toStruct(VB vb);
    public abstract VB toBuilder(VS vs);

    public Merge<VS> merge() {
        return Merges.trivial();
    }
}
