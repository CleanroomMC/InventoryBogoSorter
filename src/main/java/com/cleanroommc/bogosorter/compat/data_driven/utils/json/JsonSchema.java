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
    JsonSchema<Integer> INT = new PrimitiveJsonSchema<>(JsonElement::getAsInt, "integer");
    JsonSchema<Short> SHORT = new PrimitiveJsonSchema<>(JsonElement::getAsShort, "integer");
    JsonSchema<Long> LONG = new PrimitiveJsonSchema<>(JsonElement::getAsLong, "integer");
    JsonSchema<Float> FLOAT = new PrimitiveJsonSchema<>(JsonElement::getAsFloat, "number");
    JsonSchema<Double> DOUBLE = new PrimitiveJsonSchema<>(JsonElement::getAsDouble, "number");
    JsonSchema<Boolean> BOOL = new PrimitiveJsonSchema<>(JsonElement::getAsBoolean, "boolean");
    JsonSchema<JsonArray> JSON_ARRAY = new PrimitiveJsonSchema<>(JsonElement::getAsJsonArray, "array");
    JsonSchema<JsonObject> JSON_OBJECT = new PrimitiveJsonSchema<>(JsonElement::getAsJsonObject, "object");

    static <T> JsonSchema<T> lazy(Supplier<JsonSchema<T>> supplier) {
        return new LazyJsonSchema<>(supplier);
    }

    static <T> JsonSchema<T> dispatch(Map<String, ? extends JsonSchema<? extends T>> schemas, String dispatchKey, JsonSchema<T> fallback) {
        return new DispatchJsonSchema<>(Objects.requireNonNull(schemas), Objects.requireNonNull(dispatchKey), fallback);
    }

    static <T> JsonSchema<T> dispatch(Map<String, ? extends JsonSchema<? extends T>> schemas) {
        return dispatch(schemas, "type", null);
    }

    static <I0, O> JsonSchema<O> object(ObjectSchemaComponent<I0> i0, ObjectJsonSchema.Combiner1<I0, O> combiner) {
        return new ObjectJsonSchema<>(combiner, i0);
    }

    static <I0, I1, O> JsonSchema<O> object(
        ObjectSchemaComponent<I0> i0,
        ObjectSchemaComponent<I1> i1,
        ObjectJsonSchema.Combiner2<I0, I1, O> combiner
    ) {
        return new ObjectJsonSchema<>(combiner, i0, i1);
    }

    static <I0, I1, I2, O> JsonSchema<O> object(
        ObjectSchemaComponent<I0> i0,
        ObjectSchemaComponent<I1> i1,
        ObjectSchemaComponent<I2> i2,
        ObjectJsonSchema.Combiner3<I0, I1, I2, O> combiner
    ) {
        return new ObjectJsonSchema<>(combiner, i0, i1, i2);
    }

    static <I0, I1, I2, I3, O> JsonSchema<O> object(
        ObjectSchemaComponent<I0> i0,
        ObjectSchemaComponent<I1> i1,
        ObjectSchemaComponent<I2> i2,
        ObjectSchemaComponent<I3> i3,
        ObjectJsonSchema.Combiner4<I0, I1, I2, I3, O> combiner
    ) {
        return new ObjectJsonSchema<>(combiner, i0, i1, i2, i3);
    }

    static <I0, I1, I2, I3, I4, O> JsonSchema<O> object(
        ObjectSchemaComponent<I0> i0,
        ObjectSchemaComponent<I1> i1,
        ObjectSchemaComponent<I2> i2,
        ObjectSchemaComponent<I3> i3,
        ObjectSchemaComponent<I4> i4,
        ObjectJsonSchema.Combiner5<I0, I1, I2, I3, I4, O> combiner
    ) {
        return new ObjectJsonSchema<>(combiner, i0, i1, i2, i3, i4);
    }

    static <I0, I1, I2, I3, I4, I5, O> JsonSchema<O> object(
        ObjectSchemaComponent<I0> i0,
        ObjectSchemaComponent<I1> i1,
        ObjectSchemaComponent<I2> i2,
        ObjectSchemaComponent<I3> i3,
        ObjectSchemaComponent<I4> i4,
        ObjectSchemaComponent<I5> i5,
        ObjectJsonSchema.Combiner6<I0, I1, I2, I3, I4, I5, O> combiner
    ) {
        return new ObjectJsonSchema<>(combiner, i0, i1, i2, i3, i4, i5);
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

    default <T2> JsonSchema<T2> map(Function<? super T, ? extends T2> mapper) {
        Objects.requireNonNull(mapper);
        return new RemapJsonSchema<>(this, mapper);
    }

    default JsonSchema<T> describe(String description) {
        return new DescribingJsonSchema<>(this, null, description, null, null);
    }

    default JsonSchema<T> describe(String title, String description) {
        return new DescribingJsonSchema<>(this, title, description, null, null);
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
        return new ObjectSchemaComponent<>(this.map(Optional::of), name, true, Optional.empty());
    }
}
