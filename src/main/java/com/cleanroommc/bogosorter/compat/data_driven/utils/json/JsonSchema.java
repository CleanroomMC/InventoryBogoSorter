package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

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

    default <C extends Collection<T>> JsonSchema<C> toCollection(IntFunction<C> collectionProvider) {
        return new CollectionJsonSchema<>(this, collectionProvider);
    }

    default JsonSchema<List<T>> toList() {
        return toCollection(ArrayList::new);
    }
}
