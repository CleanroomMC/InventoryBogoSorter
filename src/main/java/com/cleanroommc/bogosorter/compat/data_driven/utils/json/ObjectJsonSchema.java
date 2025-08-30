package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author ZZZank
 */
public class ObjectJsonSchema<T> implements JsonSchema<T> {
    private final ObjectSchemaComponent<?>[] components;
    private final Combiner<T> combiner;

    private ObjectJsonSchema(Combiner<T> combiner, ObjectSchemaComponent<?>... components) {
        this.components = components;
        this.combiner = combiner;
    }

    @Override
    public T read(JsonElement json) {
        var obj = json.getAsJsonObject();

        var args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            var component = components[i];
            var got = obj.get(component.id());

            Object parsed;
            if (got == null) {
                if (!component.optional()) {
                    throw new IllegalArgumentException(String.format("Required json member with id '%s' not found", component.id()));
                }
                parsed = component.fallback();
            } else {
                parsed = component.schema().read(got);
            }

            args[i] = parsed;
        }

        return combiner.applyUnsafe(args);
    }

    @Override
    public JsonObject getSchema() {
        var obj = new JsonObject();
        obj.addProperty("type", "object");

        {
            var properties = new JsonObject();
            for (var component : components) {
                properties.add(component.id(), component.schema().getSchema());
            }
            obj.add("properties", properties);
        }

        {
            var required = new JsonArray();
            for (var component : components) {
                if (!component.optional()) {
                    required.add(component.id());
                }
            }
            obj.add("required", required);
        }

        return obj;
    }

    interface Combiner<O> {
        O applyUnsafe(Object... args);
    }

    public interface Combiner1<I0, O> extends Combiner<O> {

        O apply(I0 i0);

        @SuppressWarnings("unchecked")
        @Override
        default O applyUnsafe(Object... args) {
            return apply((I0) args[0]);
        }
    }

    public static <I0, O> ObjectJsonSchema<O> of(ObjectSchemaComponent<I0> i0, Combiner1<I0, O> combiner) {
        return new ObjectJsonSchema<>(combiner, i0);
    }

    public interface Combiner2<I0, I1, O> extends Combiner<O> {

        O apply(I0 i0, I1 i1);

        @SuppressWarnings("unchecked")
        @Override
        default O applyUnsafe(Object... args) {
            return apply((I0) args[0], (I1) args[1]);
        }
    }

    public static <I0, I1, O> ObjectJsonSchema<O> of(ObjectSchemaComponent<I0> i0, ObjectSchemaComponent<I1> i1, Combiner2<I0, I1, O> combiner) {
        return new ObjectJsonSchema<>(combiner, i0, i1);
    }

    public interface Combiner3<I0, I1, I2, O> extends Combiner<O> {

        O apply(I0 i0, I1 i1, I2 i2);

        @SuppressWarnings("unchecked")
        @Override
        default O applyUnsafe(Object... args) {
            return apply((I0) args[0], (I1) args[1], (I2) args[2]);
        }
    }

    public static <I0, I1, I2, O> ObjectJsonSchema<O> of(ObjectSchemaComponent<I0> i0, ObjectSchemaComponent<I1> i1, ObjectSchemaComponent<I2> i2, Combiner3<I0, I1, I2, O> combiner) {
        return new ObjectJsonSchema<>(combiner, i0, i1, i2);
    }

    public interface Combiner4<I0, I1, I2, I3, O> extends Combiner<O> {

        O apply(I0 i0, I1 i1, I2 i2, I3 i3);

        @SuppressWarnings("unchecked")
        @Override
        default O applyUnsafe(Object... args) {
            return apply((I0) args[0], (I1) args[1], (I2) args[2], (I3) args[3]);
        }
    }

    public static <I0, I1, I2, I3, O> ObjectJsonSchema<O> of(ObjectSchemaComponent<I0> i0, ObjectSchemaComponent<I1> i1, ObjectSchemaComponent<I2> i2, ObjectSchemaComponent<I3> i3, Combiner4<I0, I1, I2, I3, O> combiner) {
        return new ObjectJsonSchema<>(combiner, i0, i1, i2, i3);
    }

    public interface Combiner5<I0, I1, I2, I3, I4, O> extends Combiner<O> {

        O apply(I0 i0, I1 i1, I2 i2, I3 i3, I4 i4);

        @SuppressWarnings("unchecked")
        @Override
        default O applyUnsafe(Object... args) {
            return apply((I0) args[0], (I1) args[1], (I2) args[2], (I3) args[3], (I4) args[4]);
        }
    }

    public static <I0, I1, I2, I3, I4, O> ObjectJsonSchema<O> of(ObjectSchemaComponent<I0> i0, ObjectSchemaComponent<I1> i1, ObjectSchemaComponent<I2> i2, ObjectSchemaComponent<I3> i3, ObjectSchemaComponent<I4> i4, Combiner5<I0, I1, I2, I3, I4, O> combiner) {
        return new ObjectJsonSchema<>(combiner, i0, i1, i2, i3, i4);
    }

    public interface Combiner6<I0, I1, I2, I3, I4, I5, O> extends Combiner<O> {

        O apply(I0 i0, I1 i1, I2 i2, I3 i3, I4 i4, I5 i5);

        @SuppressWarnings("unchecked")
        @Override
        default O applyUnsafe(Object... args) {
            return apply((I0) args[0], (I1) args[1], (I2) args[2], (I3) args[3], (I4) args[4], (I5) args[5]);
        }
    }

    public static <I0, I1, I2, I3, I4, I5, O> ObjectJsonSchema<O> of(ObjectSchemaComponent<I0> i0, ObjectSchemaComponent<I1> i1, ObjectSchemaComponent<I2> i2, ObjectSchemaComponent<I3> i3, ObjectSchemaComponent<I4> i4, ObjectSchemaComponent<I5> i5, Combiner6<I0, I1, I2, I3, I4, I5, O> combiner) {
        return new ObjectJsonSchema<>(combiner, i0, i1, i2, i3, i4, i5);
    }
}
