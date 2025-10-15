package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * @author ZZZank
 */
@Desugar
record AsDefinitionJsonSchema<T>(
    JsonSchema<T> inner,
    String refKey
) implements JsonSchema<T> {
    @Override
    public T read(JsonElement json) {
        return inner.read(json);
    }

    @Override
    public JsonObject getSchema(Map<String, JsonSchema<?>> definitions) {
        definitions.put(refKey, inner);

        var result = new JsonObject();
        result.addProperty("$ref", "#/definitions/" + refKey);
        return result;
    }
}
