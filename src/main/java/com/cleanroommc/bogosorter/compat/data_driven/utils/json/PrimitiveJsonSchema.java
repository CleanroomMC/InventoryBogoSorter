package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
@Desugar
public record PrimitiveJsonSchema<T>(
    Function<JsonElement, T> reader,
    String type
) implements JsonSchema<T> {

    @Override
    public T read(JsonElement json) {
        return reader.apply(json);
    }

    @Override
    public JsonObject getSchema(Map<String, Supplier<JsonObject>> definitions) {
        var jsonObject = new JsonObject();
        jsonObject.addProperty("type", type);
        return jsonObject;
    }
}
