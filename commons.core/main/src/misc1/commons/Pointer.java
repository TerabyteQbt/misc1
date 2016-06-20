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
package misc1.commons;

public final class Pointer<V> {
    public final V value;

    public Pointer(V value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Pointer)) {
            return false;
        }
        Pointer<?> other = (Pointer<?>)obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        return "Pointer(" + value + ")";
    }

    public static <V> Pointer<V> of(V value) {
        return new Pointer<V>(value);
    }
}
