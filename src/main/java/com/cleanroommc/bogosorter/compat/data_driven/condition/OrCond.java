package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.github.bsideup.jabel.Desugar;

import java.util.List;

/**
 * @author ZZZank
 */
@Desugar
record OrCond(List<BogoCondition> conditions) implements BogoCondition {
    public static final JsonSchema<AndCond> SCHEMA = JsonSchema.object(
        BogoCondition.SCHEMA.toList().toField("conditions"),
        AndCond::new
    ).describe("Return `true` if any of the sub conditions returned `true`");

    @Override
    public boolean test() {
        return conditions.stream().anyMatch(BogoCondition::test);
    }
}
