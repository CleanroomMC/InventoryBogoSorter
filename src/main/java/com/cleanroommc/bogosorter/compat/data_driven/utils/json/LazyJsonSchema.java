package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
final class LazyJsonSchema<T> implements JsonSchema<T>, Supplier<JsonSchema<T>> {
    private volatile Object supplierOrInstance;

    public LazyJsonSchema(Supplier<JsonSchema<T>> supplier) {
        this.supplierOrInstance = supplier;
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
        if (supplierOrInstance instanceof Supplier<?>) {
            synchronized (this) {
                if (supplierOrInstance instanceof Supplier<?> supplier) {
                    supplierOrInstance = supplier.get();
                }
            }
        }
        return (JsonSchema<T>) supplierOrInstance;
    }
}
