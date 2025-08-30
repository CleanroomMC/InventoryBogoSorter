package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

/**
 * @author ZZZank
 */
public record ObjectSchemaComponent<T>(
    JsonSchema<T> schema,
    String id,
    boolean optional,
    T fallback
) {
}
