package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.github.bsideup.jabel.Desugar;

/**
 * @author ZZZank
 */
@Desugar
record NotCond(BogoCondition condition) implements BogoCondition {
    public static final JsonSchema<NotCond> SCHEMA = JsonSchema.object(
        BogoCondition.SCHEMA.toField("condition"),
        NotCond::new
    );

    @Override
    public boolean test() {
        return !condition.test();
    }
}
