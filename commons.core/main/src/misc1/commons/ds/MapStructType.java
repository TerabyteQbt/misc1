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
import misc1.commons.Maybe;
import misc1.commons.merge.Merge;
import misc1.commons.merge.Merges;
import org.apache.commons.lang3.tuple.Triple;

public abstract class MapStructType<S extends MapStruct<S, B, K, VS, VB>, B, K, VS, VB> {
    protected abstract S create(ImmutableMap<K, VS> map);
    protected abstract B createBuilder(ImmutableSalvagingMap<K, VB> map);
    protected abstract VS toStruct(VB vb);
    protected abstract VB toBuilder(VS vs);

    protected Merge<VS> mergeValue() {
        return Merges.trivial();
    }

    public Merge<S> merge() {
        return (lhs, mhs, rhs) -> {
            Merge<VS> mergeValue = mergeValue();
            Merge<Maybe<VS>> mergeMaybeValue = Merges.maybe(mergeValue);
            Merge<ImmutableMap<K, VS>> mergeMap = Merges.<K, VS>map(mergeMaybeValue);
            Triple<ImmutableMap<K, VS>, ImmutableMap<K, VS>, ImmutableMap<K, VS>> r = mergeMap.merge(lhs.map, mhs.map, rhs.map);
            S lhs2 = create(r.getLeft());
            S mhs2 = create(r.getMiddle());
            S rhs2 = create(r.getRight());
            return Triple.of(lhs2, mhs2, rhs2);
        };
    }

    public B builder() {
        return createBuilder(ImmutableSalvagingMap.<K, VB>of());
    }
}
