package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.google.gson.JsonObject;

/**
 * @author ZZZank
 */
public enum ConstantCond implements BogoCondition {
    ALWAYS(true),
    NEVER(false);

    public static ConstantCond read(JsonObject object) {
        return of(object.get("value").getAsBoolean());
    }

    public static ConstantCond of(boolean value) {
        return value ? ALWAYS : NEVER ;
    }

    private final boolean value;

    ConstantCond(boolean value) {
        this.value = value;
    }

    @Override
    public boolean test() {
        return value;
    }
}
