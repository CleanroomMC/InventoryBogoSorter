package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * @author ZZZank
 */
@Desugar
public record OrCond(List<BogoCondition> conditions) implements BogoCondition {
    public static OrCond read(JsonObject object) {
        return new OrCond(BogoCondition.readList(object.get("value").getAsJsonArray()));
    }

    @Override
    public boolean test() {
        return conditions.stream().anyMatch(BogoCondition::test);
    }
}
