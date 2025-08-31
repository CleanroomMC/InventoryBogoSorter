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
record RemapJsonSchema<I, O>(
    JsonSchema<I> inner,
    Function<I, O> remapper
) implements JsonSchema<O> {
    @Override
    public O read(JsonElement json) {
        return remapper.apply(inner.read(json));
    }

    @Override
    public JsonObject getSchema(Map<String, Supplier<JsonObject>> definitions) {
        return inner.getSchema(definitions);
    }
}
