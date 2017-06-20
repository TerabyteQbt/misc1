package misc1.commons.ds;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import misc1.commons.Maybe;
import misc1.commons.json.JsonSerializer;
import misc1.commons.json.JsonSerializers;
import misc1.commons.json.StringSerializer;
import misc1.commons.merge.Merge;
import misc1.commons.merge.Merges;
import org.apache.commons.lang3.tuple.Triple;

public abstract class MapStructType<S extends MapStruct<S, B, K, VS, VB>, B extends MapStructBuilder<S, B, K, VS, VB>, K, VS, VB> {
    protected abstract S create(ImmutableMap<K, VS> map);
    protected abstract B createBuilder(ImmutableSalvagingMap<K, VB> map);
    protected abstract VS toStruct(VB vb);
    protected abstract VB toBuilder(VS vs);

    protected Merge<VS> mergeValue() {
        return Merges.trivial();
    }

    protected Optional<StringSerializer<K>> keySerializer() {
        return Optional.absent();
    }

    protected Optional<JsonSerializer<VB>> valueSerializer() {
        return Optional.absent();
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

    Optional<JsonSerializer<B>> serializerBOptional() {
        Optional<StringSerializer<K>> keySerializerOptional = keySerializer();
        if(!keySerializerOptional.isPresent()) {
            return Optional.absent();
        }
        StringSerializer<K> keySerializer = keySerializerOptional.get();

        Optional<JsonSerializer<VB>> valueSerializerOptional = valueSerializer();
        if(!valueSerializerOptional.isPresent()) {
            return Optional.absent();
        }
        JsonSerializer<VB> valueSerializer = valueSerializerOptional.get();

        return Optional.of(new JsonSerializer<B>() {
            @Override
            public JsonElement toJson(B b) {
                JsonObject r = new JsonObject();
                for(Map.Entry<K, VB> e : b.map.entries()) {
                    r.add(keySerializer.toString(e.getKey()), valueSerializer.toJson(e.getValue()));
                }
                return r;
            }

            @Override
            public B fromJson(JsonElement el) {
                B b = builder();
                for(Map.Entry<String, JsonElement> e: el.getAsJsonObject().entrySet()) {
                    b = b.with(keySerializer.fromString(e.getKey()), valueSerializer.fromJson(e.getValue()));
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

    public B builder() {
        return createBuilder(ImmutableSalvagingMap.<K, VB>of());
    }
}
