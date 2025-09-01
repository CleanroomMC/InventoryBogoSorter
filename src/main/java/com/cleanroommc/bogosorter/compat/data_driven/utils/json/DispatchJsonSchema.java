package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.cleanroommc.bogosorter.compat.data_driven.utils.DataDrivenUtils;
import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.function.Supplier;

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
    public JsonObject getSchema(Map<String, Supplier<JsonObject>> definitions) {
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
            var oneOf = new JsonArray();
            for (var entry : schemas.entrySet()) {
                var schema = entry.getValue().getSchema(definitions);

                oneOf.add(DataDrivenUtils.mergeJson(schema, buildDispatchJson(entry.getKey())));
            }
            obj.add("oneOf", oneOf);
        }

        {
            var required = new JsonArray();
            required.add(this.dispatchKey);
            obj.add("required", required);
        }

        return obj;
    }

    private JsonObject buildDispatchJson(String dispatchValue) {
        var result = new JsonObject();

        {
            var required = new JsonArray();
            required.add(dispatchKey);
            result.add("required", required);
        }

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
