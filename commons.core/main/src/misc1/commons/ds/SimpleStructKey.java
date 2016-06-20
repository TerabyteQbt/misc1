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

public class SimpleStructKey<S, V> extends StructKey<S, V, V> {
    public SimpleStructKey(String name) {
        super(name);
    }

    public SimpleStructKey(String name, V v) {
        super(name, v);
    }

    @Override
    public V toStruct(V v) {
        return v;
    }

    @Override
    public V toBuilder(V v) {
        return v;
    }
}
