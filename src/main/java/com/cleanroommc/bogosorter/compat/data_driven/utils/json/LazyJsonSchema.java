package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
final class LazyJsonSchema<T> implements JsonSchema<T>, Supplier<JsonSchema<T>> {
    private volatile Object supplierOrInstance;
    private volatile boolean initialized;

    public LazyJsonSchema(Supplier<JsonSchema<T>> supplier) {
        this.supplierOrInstance = Objects.requireNonNull(supplier);
    }

    @Override
    public T read(JsonElement json) {
        return get().read(json);
    }

    @Override
    public JsonObject getSchema(Map<String, Supplier<JsonObject>> definitions) {
        return get().getSchema(definitions);
    }

    @Override
    public JsonSchema<T> get() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    initialized = true;
                    supplierOrInstance = ((Supplier<?>) supplierOrInstance).get();
                }
            }
        }
        return (JsonSchema<T>) supplierOrInstance;
    }
}
