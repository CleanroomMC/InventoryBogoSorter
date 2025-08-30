package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.cleanroommc.bogosorter.compat.data_driven.utils.json.DispatchJsonSchema;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.ObjectJsonSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZZZank
 */
public interface BogoCondition {
    Map<String, ObjectJsonSchema<? extends BogoCondition>> REGISTRY = new HashMap<>();
    JsonSchema<BogoCondition> SCHEMA = JsonSchema.lazy(() -> {
        REGISTRY.put("and", AndCond.SCHEMA);
        REGISTRY.put("or", OrCond.SCHEMA);
        REGISTRY.put("not", NotCond.SCHEMA);
        REGISTRY.put("mod", ModCond.SCHEMA);
        REGISTRY.put("constant", ConstantCond.SCHEMA);
        return new DispatchJsonSchema<>(REGISTRY, "type", null);
    });

    boolean test();
}
