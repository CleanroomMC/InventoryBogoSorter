package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.github.bsideup.jabel.Desugar;

import java.util.List;

/**
 * @author ZZZank
 */
@Desugar
record AndCond(List<BogoCondition> conditions) implements BogoCondition {
    public static final JsonSchema<AndCond> SCHEMA = JsonSchema.object(
        BogoCondition.SCHEMA.toList().toField("conditions"),
        AndCond::new
    );

    @Override
    public boolean test() {
        return conditions.stream().allMatch(BogoCondition::test);
    }
}
