package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * @author ZZZank
 */
public record DispatchJsonSchema<T>(
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
    public JsonObject getSchema() {
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
            var anyOf = new JsonArray();
            for (var entry : schemas.entrySet()) {
                var statement = new JsonObject();
                statement.add("if", buildIfStatement(entry.getKey()));
                statement.add("then", entry.getValue().getSchema());
                anyOf.add(statement);
            }
            obj.add("anyOf", anyOf);
        }

        return obj;
    }

    private JsonObject buildIfStatement(String requiredValue) {
        var if_ = new JsonObject();
        {
            var properties = new JsonObject();
            {
                var value = new JsonObject();
                value.addProperty("const", requiredValue);
                properties.add(dispatchKey, value);
            }
            if_.add("properties", properties);
        }
        return if_;
    }
}
