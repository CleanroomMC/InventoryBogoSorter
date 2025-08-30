package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

/**
 * @author ZZZank
 */
public record PrimitiveJsonSchema<T>(
    Function<JsonElement, T> reader,
    String type
) implements JsonSchema<T> {

    @Override
    public T read(JsonElement json) {
        return reader.apply(json);
    }

    @Override
    public JsonObject getSchema() {
        var jsonObject = new JsonObject();
        jsonObject.addProperty("type", type);
        return jsonObject;
    }
}
