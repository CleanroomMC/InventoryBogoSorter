package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZZank
 */
public interface BogoCondition {
    String TYPE_KEY = "type";

    static BogoCondition read(JsonObject object) {
        return switch (object.get(TYPE_KEY).getAsString()) {
            case "mod" -> ModCond.read(object);
            case "not" -> NotCond.read(object);
            case "and" -> AndCond.read(object);
            case "or" -> OrCond.read(object);
            case "constant" -> ConstantCond.read(object);
            default -> throw new IllegalStateException("Unexpected condition type: " + object.get(TYPE_KEY));
        };
    }

    static @NotNull List<BogoCondition> readList(JsonArray array) {
        var parsed = new ArrayList<BogoCondition>(array.size());
        for (var element : array) {
            parsed.add(BogoCondition.read(element.getAsJsonObject()));
        }
        return parsed;
    }

    boolean test();
}
