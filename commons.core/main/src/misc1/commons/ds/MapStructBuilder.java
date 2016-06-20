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
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class MapStructBuilder<S extends MapStruct<S, B, K, VS, VB>, B, K, VS, VB> {
    private final MapStructType<S, B, K, VS, VB> type;
    public final ImmutableSalvagingMap<K, VB> map;

    protected MapStructBuilder(MapStructType<S, B, K, VS, VB> type, ImmutableSalvagingMap<K, VB> map) {
        this.type = type;
        this.map = map;
    }

    public VB get(K key) {
        return map.get(key);
    }

    public B with(K key, VB vb) {
        return type.createBuilder(map.simplePut(key, vb));
    }

    public B without(K key) {
        return type.createBuilder(map.simpleRemove(key));
    }

    public B transform(K k, Function<VB, VB> f) {
        return with(k, f.apply(get(k)));
    }

    public S build() {
        ImmutableMap.Builder<K, VS> b = ImmutableMap.builder();
        for(Map.Entry<K, VB> e : map.entries()) {
            b.put(e.getKey(), type.toStruct(e.getValue()));
        }
        return type.create(b.build());
    }

    @Override
    public final int hashCode() {
        return map.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!getClass().equals(obj.getClass())) {
            return false;
        }
        return map.equals(((MapStructBuilder) obj).map);
    }
}
