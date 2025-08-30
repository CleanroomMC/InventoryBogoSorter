package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author ZZZank
 */
public interface JsonSchema<T> {
    JsonSchema<String> STRING = new PrimitiveJsonSchema<>(JsonElement::getAsString, "string");
    JsonSchema<Integer> INT = new PrimitiveJsonSchema<>(JsonElement::getAsInt, "number");
    JsonSchema<Short> SHORT = new PrimitiveJsonSchema<>(JsonElement::getAsShort, "number");
    JsonSchema<Long> LONG = new PrimitiveJsonSchema<>(JsonElement::getAsLong, "number");
    JsonSchema<Float> FLOAT = new PrimitiveJsonSchema<>(JsonElement::getAsFloat, "number");
    JsonSchema<Double> DOUBLE = new PrimitiveJsonSchema<>(JsonElement::getAsDouble, "number");
    JsonSchema<Boolean> BOOL = new PrimitiveJsonSchema<>(JsonElement::getAsBoolean, "boolean");

    T read(JsonElement json);

    JsonObject getSchema();

    default <T2> JsonSchema<T2> map(Function<T, T2> mapper) {
        Objects.requireNonNull(mapper);
        return new RemapJsonSchema<>(this, mapper);
    }

    default JsonSchema<T> describe(String description) {
        return new DescribingJsonSchema<>(this, null, description, null, null);
    }

    default JsonSchema<T> describe(String title, String description, String examples, String $comment) {
        return new DescribingJsonSchema<>(this, title, description, examples, $comment);
    }
}
