package com.cleanroommc.bogosorter.compat.data_driven.utils;

import com.cleanroommc.bogosorter.compat.data_driven.BogoCompatParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZZZank
 */
public interface JsonTernary {

    String KEY_IF = "$if";
    String KEY_IF_TRUE = "matched";
    String KEY_IF_FALSE = "fallback";

    /**
     * @return result of ternary {@link #get(JsonObject)} if provided {@link JsonElement} matched the pattern specified
     * in the javadoc in {@link #get(JsonObject)}, or the element itself otherwise
     */
    static JsonElement getOrSelf(@Nullable JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return element;
        }
        final var unwrapped = get(element.getAsJsonObject());
        return unwrapped == null ? element : unwrapped;
    }

    /**
     * {
     *     "$if": "{@link com.cleanroommc.bogosorter.compat.data_driven.BogoCondition}, and it will be evaluated immediately",
     *     "t": "this will be return if condition is 'true'",
     *     "f": "this will be return if condition is 'false'"
     * }
     */
    @Nullable
    static JsonElement get(@NotNull JsonObject o) {
        final var condition = o.get(KEY_IF);
        final var ifTrue = o.get(KEY_IF_TRUE);
        final var ifFalse = o.get(KEY_IF_FALSE);
        if (condition == null || !condition.isJsonObject() || ifTrue == null || ifFalse == null) {
            return null;
        }
        return BogoCompatParser.parseCondition(condition.getAsJsonObject()).test()
            ? ifTrue
            : ifFalse;
    }
}
