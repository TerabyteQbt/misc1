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

import com.google.common.collect.ImmutableMap;

public class Struct<S extends Struct<S, B>, B extends StructBuilder<S, B>> {
    private final StructType<S, B> type;
    private final ImmutableMap<StructKey<S, ?, ?>, Object> map;

    protected Struct(StructType<S, B> type, ImmutableMap<StructKey<S, ?, ?>, Object> map) {
        this.type = type;
        this.map = map;
    }

    public <VS> VS get(StructKey<S, VS, ?> k) {
        return (VS)map.get(k);
    }

    public <VB> S set(StructKey<S, ?, VB> k, VB vb) {
        return builder().set(k, vb).build();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + map.toString();
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!getClass().equals(obj.getClass())) {
            return false;
        }
        Struct<S, B> other = (Struct<S, B>)obj;
        return map.equals(other.map);
    }

    public B builder() {
        B b = type.builder();
        for(StructKey<S, ?, ?> k : map.keySet()) {
            b = copyKey(b, k);
        }
        return b;
    }

    private <VS, VB> B copyKey(B b, StructKey<S, VS, VB> k) {
        VS vs = get(k);
        return b.set(k, k.toBuilder(vs));
    }
}
