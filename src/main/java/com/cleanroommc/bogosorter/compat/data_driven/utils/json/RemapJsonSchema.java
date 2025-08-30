package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

/**
 * @author ZZZank
 */
public record RemapJsonSchema<I, O>(
    JsonSchema<I> inner,
    Function<I, O> remapper
) implements JsonSchema<O> {
    @Override
    public O read(JsonElement json) {
        return remapper.apply(inner.read(json));
    }

    @Override
    public JsonObject getSchema() {
        return inner.getSchema();
    }
}
