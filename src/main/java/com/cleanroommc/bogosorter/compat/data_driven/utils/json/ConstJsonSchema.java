package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * @author ZZZank
 */
@Desugar
record ConstJsonSchema<T>(T value, JsonElement jsonRepresentation) implements JsonSchema<T> {
    @Override
    public T read(JsonElement json) {
        return value;
    }

    @Override
    public JsonObject getSchema(Map<String, JsonSchema<?>> definitions) {
        var result = new JsonObject();
        if (this.jsonRepresentation != null) {
            result.add("const", jsonRepresentation);
        }
        return result;
    }
}
