package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonObject;

/**
 * @author ZZZank
 */
@Desugar
public record NotCond(BogoCondition condition) implements BogoCondition {
    public static NotCond read(JsonObject object) {
        return new NotCond(BogoCondition.read(object.get("value").getAsJsonObject()));
    }

    @Override
    public boolean test() {
        return !condition.test();
    }
}
