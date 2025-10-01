package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * @author ZZZank
 */
@Desugar
record DispatchJsonSchema<T>(
    Map<String, ? extends JsonSchema<? extends T>> schemas,
    String dispatchKey,
    JsonSchema<T> fallback
) implements JsonSchema<T> {

    @Override
    public T read(JsonElement json) {
        var object = json.getAsJsonObject();
        var dispatch = object.get(dispatchKey);
        if (dispatch == null) {
            if (fallback == null) {
                throw new IllegalArgumentException(String.format("Dispatch key '%s' not found", dispatchKey));
            }
            return fallback.read(json);
        }
        return schemas.get(dispatch.getAsString()).read(json);
    }

    @Override
    public JsonObject getSchema(Map<String, JsonSchema<?>> definitions) {
        var obj = new JsonObject();
        obj.addProperty("type", "object");

        {
            var properties = new JsonObject();

            var dispatch = new JsonObject();
            dispatch.addProperty("type", "string");

            var allowed = new JsonArray();
            schemas.keySet().forEach(allowed::add);
            dispatch.add("enum", allowed);

            properties.add(dispatchKey, dispatch);
            obj.add("properties", properties);
        }

        {
            var required = new JsonArray();
            required.add(this.dispatchKey);
            obj.add("required", required);
        }

        {
            var oneOf = new JsonArray();
            for (var entry : schemas.entrySet()) {
                var match = new JsonObject();

                match.add("if", buildIfStatement(entry.getKey()));
                match.add("then", entry.getValue().getSchema(definitions));

                oneOf.add(match);
            }
            obj.add("allOf", oneOf);
        }

        return obj;
    }

    private JsonObject buildIfStatement(String dispatchValue) {
        var result = new JsonObject();

        {
            var properties = new JsonObject();
            {
                var dispatchKeyJson = new JsonObject();
                dispatchKeyJson.addProperty("const", dispatchValue);
                properties.add(dispatchKey, dispatchKeyJson);
            }
            result.add("properties", properties);
        }

        return result;
    }
}
