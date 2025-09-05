package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
@Desugar
record DeferredJsonSchema<T>(
    JsonSchema<T> inner
) implements JsonSchema<Supplier<T>> {
    @Override
    public Supplier<T> read(JsonElement json) {
        return () -> inner.read(json);
    }

    @Override
    public JsonObject getSchema(Map<String, Supplier<JsonObject>> definitions) {
        return inner.getSchema(definitions);
    }
}
