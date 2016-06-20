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

import com.google.common.base.Function;

public abstract class StructBuilder<S extends Struct<S, B>, B extends StructBuilder<S, B>> {
    private final StructType<S, B> type;
    private ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> map;

    protected StructBuilder(StructType<S, B> type, ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> map) {
        this.type = type;
        this.map = map;
    }

    public <VB> B set(StructKey<S, ?, VB> k, VB vb) {
        return type.builderCtor.apply(map.simplePut(k, vb));
    }

    public <VB> VB get(StructKey<S, ?, VB> k) {
        if(map.containsKey(k)) {
            return (VB)map.get(k);
        }
        return k.getDefault().get();
    }

    public <VB> B transform(StructKey<S, ?, VB> k, Function<VB, VB> f) {
        return set(k, f.apply(get(k)));
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
        StructBuilder<S, B> other = (StructBuilder<S, B>)obj;
        return map.equals(other.map);
    }

    public B apply(Function<B, B> f) {
        return f.apply(type.builderCtor.apply(map));
    }

    public S build() {
        return type.create(map);
    }
}
