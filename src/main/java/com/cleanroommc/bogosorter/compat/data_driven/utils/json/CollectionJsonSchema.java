package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
@Desugar
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
    public JsonObject getSchema(Map<String, Supplier<JsonObject>> definitions) {
        var obj = new JsonObject();
        obj.addProperty("type", "array");
        obj.add("items", component.getSchema(definitions));
        return obj;
    }
}
