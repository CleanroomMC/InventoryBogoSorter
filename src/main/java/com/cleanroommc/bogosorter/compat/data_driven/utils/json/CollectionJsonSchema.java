package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.function.IntFunction;

/**
 * @author ZZZank
 */
public record CollectionJsonSchema<T, C extends Collection<T>>(
    JsonSchema<T> component,
    IntFunction<C> collection
) implements JsonSchema<C> {
    @Override
    public C read(JsonElement json) {
        var array = json.getAsJsonArray();

        var result = collection.apply(array.size());
        for (var element : array) {
            result.add(component.read(element));
        }
        return result;
    }

    @Override
    public JsonObject getSchema() {
        var obj = new JsonObject();
        obj.addProperty("type", "array");
        obj.add("items", component.getSchema());
        return obj;
    }
}
