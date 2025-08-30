package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.ObjectJsonSchema;

/**
 * @author ZZZank
 */
enum ConstantCond implements BogoCondition {
    ALWAYS,
    NEVER;

    public static final JsonSchema<ConstantCond> SCHEMA_SIMPLE = JsonSchema.BOOL.map(ConstantCond::of);
    public static final ObjectJsonSchema<ConstantCond> SCHEMA = ObjectJsonSchema.of(
        JsonSchema.BOOL.toField("value"),
        ConstantCond::of
    );

    public static ConstantCond of(boolean value) {
        return value ? ALWAYS : NEVER ;
    }

    public boolean test() {
        return this == ALWAYS;
    }
}
