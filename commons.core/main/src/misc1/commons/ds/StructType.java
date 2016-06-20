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
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import misc1.commons.ds.ImmutableSalvagingMap;
import misc1.commons.merge.Merge;
import org.apache.commons.lang3.tuple.Triple;

public class StructType<S extends Struct<S, B>, B extends StructBuilder<S, B>> {
    public final ImmutableList<StructKey<S, ?, ?>> keys;
    final Function<ImmutableMap<StructKey<S, ?, ?>, Object>, S> structCtor;
    final Function<ImmutableSalvagingMap<StructKey<S, ?, ?>, Object>, B> builderCtor;

    public StructType(Iterable<StructKey<S, ?, ?>> keys, Function<ImmutableMap<StructKey<S, ?, ?>, Object>, S> structCtor, Function<ImmutableSalvagingMap<StructKey<S, ?, ?>, Object>, B> builderCtor) {
        this.keys = ImmutableList.copyOf(keys);
        this.structCtor = structCtor;
        this.builderCtor = builderCtor;
    }

    public B builder() {
        ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> b = ImmutableSalvagingMap.of();
        for(StructKey<S, ?, ?> k : keys) {
            b = copyDefault(b, k);
        }
        return builderCtor.apply(b);
    }

    private static <S, VS, VB> ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> copyDefault(ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> b, StructKey<S, VS, VB> key) {
        Optional<VB> mvb = key.getDefault();
        if(mvb.isPresent()) {
            b = b.simplePut(key, mvb.get());
        }
        return b;
    }

    public final S create(ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> map) {
        for(StructKey<S, ?, ?> k : map.keys()) {
            if(!keys.contains(k)) {
                throw new IllegalArgumentException("Nonsense keys: " + k);
            }
        }
        ImmutableMap.Builder<StructKey<S, ?, ?>, Object> b = ImmutableMap.builder();
        for(StructKey<S, ?, ?> k : keys) {
            copyKey(b, map, k);
        }
        return structCtor.apply(b.build());
    }

    private static <S, VS, VB> void copyKey(ImmutableMap.Builder<StructKey<S, ?, ?>, Object> b, ImmutableSalvagingMap<StructKey<S, ?, ?>, Object> map, StructKey<S, VS, VB> k) {
        VB vb = (VB)map.get(k);
        if(vb == null) {
            throw new IllegalArgumentException("Key required: " + k);
        }
        VS vs = k.toStruct(vb);
        b.put(k, vs);
    }

    public Merge<S> merge() {
        return (lhs, mhs, rhs) -> {
            final ImmutableMap.Builder<StructKey<S, ?, ?>, Object> lhsB = ImmutableMap.builder();
            final ImmutableMap.Builder<StructKey<S, ?, ?>, Object> mhsB = ImmutableMap.builder();
            final ImmutableMap.Builder<StructKey<S, ?, ?>, Object> rhsB = ImmutableMap.builder();
            class Helper {
                private <VS, VB> void mergeKey(StructKey<S, VS, VB> k) {
                    Triple<VS, VS, VS> r = k.merge().merge(lhs.get(k), mhs.get(k), rhs.get(k));
                    lhsB.put(k, r.getLeft());
                    mhsB.put(k, r.getMiddle());
                    rhsB.put(k, r.getRight());
                }
            }
            Helper h = new Helper();
            for(StructKey<S, ?, ?> k : keys) {
                h.mergeKey(k);
            }
            S lhs2 = structCtor.apply(lhsB.build());
            S mhs2 = structCtor.apply(mhsB.build());
            S rhs2 = structCtor.apply(rhsB.build());
            return Triple.of(lhs2, mhs2, rhs2);
        };
    }
}
