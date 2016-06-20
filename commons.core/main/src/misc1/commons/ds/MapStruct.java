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
import java.util.Map;

public class MapStruct<S extends MapStruct<S, B, K, VS, VB>, B, K, VS, VB> {
    private final MapStructType<S, B, K, VS, VB> type;
    public final ImmutableMap<K, VS> map;

    protected MapStruct(MapStructType<S, B, K, VS, VB> type, ImmutableMap<K, VS> map) {
        this.type = type;
        this.map = map;
    }

    public B builder() {
        ImmutableSalvagingMap<K, VB> b = ImmutableSalvagingMap.of();
        for(Map.Entry<K, VS> e : map.entrySet()) {
            b = b.simplePut(e.getKey(), type.toBuilder(e.getValue()));
        }
        return type.createBuilder(b);
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
        return map.equals(((MapStruct) obj).map);
    }
}
