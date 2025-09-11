package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.function.Function;

/**
 * @author ZZZank
 */
@Desugar
record RemapJsonSchema<I, O>(
    JsonSchema<I> inner,
    Function<? super I, ? extends O> objectRemapper,
    Function<? super JsonObject, JsonObject> schemaRemapper
) implements JsonSchema<O> {
    @Override
    public O read(JsonElement json) {
        return objectRemapper.apply(inner.read(json));
    }

    @Override
    public JsonObject getSchema(Map<String, JsonSchema<?>> definitions) {
        return schemaRemapper.apply(inner.getSchema(definitions));
    }
}
