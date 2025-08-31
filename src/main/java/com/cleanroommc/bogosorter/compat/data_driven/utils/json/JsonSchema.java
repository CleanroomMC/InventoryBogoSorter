package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

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
    JsonSchema<JsonArray> JSON_ARRAY = new PrimitiveJsonSchema<>(JsonElement::getAsJsonArray, "array");
    JsonSchema<JsonObject> JSON_OBJECT = new PrimitiveJsonSchema<>(JsonElement::getAsJsonObject, "object");

    static <T> JsonSchema<T> lazy(Supplier<JsonSchema<T>> supplier) {
        return new LazyJsonSchema<>(supplier);
    }

    T read(JsonElement json);

    JsonObject getSchema(Map<String, Supplier<JsonObject>> definitions);

    default JsonObject getSchema() {
        var definitions = new LinkedHashMap<String, Supplier<JsonObject>>();
        var schema = getSchema(definitions);

        var definitionsJson = new JsonObject();
        for (var entry : definitions.entrySet()) {
            definitionsJson.add(entry.getKey(), entry.getValue().get());
        }
        schema.add("definitions", definitionsJson);

        return schema;
    }

    default JsonSchema<T> extractToDefinitions(String referenceKey) {
        return new AsDefinitionJsonSchema<>(this, referenceKey);
    }

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

    default ObjectSchemaComponent<T> toField(String name) {
        return new ObjectSchemaComponent<>(this, name, false, null);
    }

    default ObjectSchemaComponent<T> toOptionalField(String name, T fallback) {
        return new ObjectSchemaComponent<>(this, name, true, fallback);
    }

    default ObjectSchemaComponent<Optional<T>> toOptionalField(String name) {
        return new ObjectSchemaComponent<>(new RemapJsonSchema<>(this, Optional::of), name, true, Optional.empty());
    }
}
