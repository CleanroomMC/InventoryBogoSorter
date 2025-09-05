package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
@Desugar
final class EnumJsonSchema<T extends Enum<T>> implements JsonSchema<T> {
    private final boolean ignoreCase;
    private final boolean includeOrdinal;

    private transient final Map<String, T> named;
    private transient final List<T> indexed;

    EnumJsonSchema(Class<T> type, boolean ignoreCase, boolean includeOrdinal) {
        this.ignoreCase = ignoreCase;
        this.includeOrdinal = includeOrdinal;

        var enumConstants = type.getEnumConstants();
        this.named = new HashMap<>();
        for (var enumConstant : enumConstants) {
            named.put(ignoreCase ? enumConstant.name().toLowerCase(Locale.ROOT) : enumConstant.name(), enumConstant);
        }

        this.indexed = includeOrdinal ? Arrays.asList(enumConstants) : Collections.emptyList();
    }

    @Override
    public T read(JsonElement json) {
        var primitive = json.getAsJsonPrimitive();
        if (includeOrdinal && primitive.isNumber()) {
            return indexed.get(primitive.getAsInt());
        }

        var name = primitive.getAsString();
        if (ignoreCase) {
            name = name.toLowerCase(Locale.ROOT);
        }
        return Objects.requireNonNull(named.get(name), "No enum found");
    }

    @Override
    public JsonObject getSchema(Map<String, Supplier<JsonObject>> definitions) {
        var result = new JsonObject();

        var enum_ = new JsonArray();
        for (var name : named.keySet()) {
            enum_.add(name);
        }
        for (int i = 0, len = indexed.size(); i < len; i++) {
            enum_.add(i);
        }
        result.add("enum", enum_);

        return result;
    }
}
