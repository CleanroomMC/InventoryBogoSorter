package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;

/**
 * @author ZZZank
 */
enum ConstantCond implements BogoCondition {
    ALWAYS,
    NEVER;

    public static final JsonSchema<ConstantCond> SCHEMA_SIMPLE = JsonSchema.BOOL.map(ConstantCond::of);
    public static final JsonSchema<ConstantCond> SCHEMA = JsonSchema.object(
        JsonSchema.BOOL.toField("value"),
        ConstantCond::of
    ).describe("Return the value in 'value' field");

    public static ConstantCond of(boolean value) {
        return value ? ALWAYS : NEVER ;
    }

    public boolean test() {
        return this == ALWAYS;
    }
}
