package misc1.commons.json;

import com.google.gson.JsonElement;

public interface JsonSerializer<V> {
    JsonElement toJson(V v);
    V fromJson(JsonElement e);
}
