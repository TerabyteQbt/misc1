package misc1.commons.ds;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.function.BiFunction;
import misc1.commons.ds.ImmutableSalvagingMap;
import misc1.commons.json.JsonSerializer;
import misc1.commons.json.JsonSerializers;
import misc1.commons.merge.Merge;
import org.apache.commons.lang3.tuple.Triple;

public class StructType<S extends Struct<S, B>, B extends StructBuilder<S, B>> {
    public final ImmutableList<StructKey<S, ?, ?>> keys;
    final Function<ImmutableMap<StructKey<S, ?, ?>, Object>, S> structCtor;
    final Function<ImmutableSalvagingMap<StructKey<S, ?, ?>, Object>, B> builderCtor;

    StructType(Iterable<StructKey<S, ?, ?>> keys, Function<ImmutableMap<StructKey<S, ?, ?>, Object>, S> structCtor, Function<ImmutableSalvagingMap<StructKey<S, ?, ?>, Object>, B> builderCtor) {
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
        if(key.def.isPresent()) {
            b = b.simplePut(key, key.def.get());
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
        VS vs = k.toStruct.apply(vb);
        b.put(k, vs);
    }

    public Merge<S> merge() {
        return (lhs, mhs, rhs) -> {
            final ImmutableMap.Builder<StructKey<S, ?, ?>, Object> lhsB = ImmutableMap.builder();
            final ImmutableMap.Builder<StructKey<S, ?, ?>, Object> mhsB = ImmutableMap.builder();
            final ImmutableMap.Builder<StructKey<S, ?, ?>, Object> rhsB = ImmutableMap.builder();
            class Helper {
                private <VS, VB> void mergeKey(StructKey<S, VS, VB> k) {
                    Triple<VS, VS, VS> r = k.merge.merge(lhs.get(k), mhs.get(k), rhs.get(k));
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

    Optional<JsonSerializer<B>> serializerBOptional() {
        ImmutableMap.Builder<String, Function<B, JsonElement>> serStepsBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<String, BiFunction<B, JsonElement, B>> deserStepsBuilder = ImmutableMap.builder();
        ImmutableList.Builder<String> broken = ImmutableList.builder();

        class Helper {
            private <VS, VB> void checkKey(StructKey<S, VS, VB> k) {
                Optional<JsonSerializer<VB>> serializerOptional = k.serializer;
                if(!serializerOptional.isPresent()) {
                    broken.add(k.name);
                    return;
                }
                JsonSerializer<VB> serializer = serializerOptional.get();
                serStepsBuilder.put(k.name, (b) -> {
                    VB vb = b.get(k);
                    if(k.def.isPresent() && k.def.get().equals(vb)) {
                        return null;
                    }
                    return serializer.toJson(vb);
                });
                deserStepsBuilder.put(k.name, (b, el) -> {
                    return b.set(k, serializer.fromJson(el));
                });
            }
        }
        Helper h = new Helper();
        for(StructKey<S, ?, ?> k : keys) {
            h.checkKey(k);
        }

        if(!broken.build().isEmpty()) {
            return Optional.absent();
        }

        ImmutableMap<String, Function<B, JsonElement>> serSteps = serStepsBuilder.build();
        ImmutableMap<String, BiFunction<B, JsonElement, B>> deserSteps = deserStepsBuilder.build();
        return Optional.of(new JsonSerializer<B>() {
            @Override
            public JsonElement toJson(B b) {
                JsonObject r = new JsonObject();
                for(Map.Entry<String, Function<B, JsonElement>> e : serSteps.entrySet()) {
                    JsonElement el = e.getValue().apply(b);
                    if(el != null) {
                        r.add(e.getKey(), el);
                    }
                }
                return r;
            }

            @Override
            public B fromJson(JsonElement el) {
                B b = builder();
                for(Map.Entry<String, JsonElement> e : el.getAsJsonObject().entrySet()) {
                    String name = e.getKey();
                    BiFunction<B, JsonElement, B> deserStep = deserSteps.get(name);
                    if(deserStep == null) {
                        throw new IllegalArgumentException("Invalid key: " + name);
                    }
                    b = deserStep.apply(b, e.getValue());
                }
                return b;
            }
        });
    }

    public JsonSerializer<B> serializerB() {
        return serializerBOptional().get();
    }

    public JsonSerializer<S> serializer() {
        return JsonSerializers.wrapper(S::builder, serializerB(), B::build);
    }
}
