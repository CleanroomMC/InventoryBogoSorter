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
public record DescribingJsonSchema<T>(
    JsonSchema<T> inner,
    String title,
    String description,
    String examples,
    String $comment
) implements JsonSchema<T> {
    @Override
    public T read(JsonElement json) {
        return inner.read(json);
    }

    @Override
    public JsonObject getSchema(Map<String, Supplier<JsonObject>> definitions) {
        var got = inner.getSchema(definitions);
        addIfNotNull(got, "title", title);
        addIfNotNull(got, "description", description);
        addIfNotNull(got, "examples", examples);
        addIfNotNull(got, "$comment", $comment);
        return got;
    }

    private void addIfNotNull(JsonObject target, String id, String value) {
        if (value != null) {
            target.addProperty(id, value);
        }
    }
}
