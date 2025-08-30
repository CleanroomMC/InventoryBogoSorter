package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author ZZZank
 */
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
    public JsonObject getSchema() {
        var got = inner.getSchema();
        addIfNotNull(got, "title", title);
        addIfNotNull(got, "description", description);
        addIfNotNull(got, "examples", examples);
        addIfNotNull(got, "$comment", $comment);
        return null;
    }

    private void addIfNotNull(JsonObject target, String id, String value) {
        if (value != null) {
            target.addProperty(id, value);
        }
    }
}
