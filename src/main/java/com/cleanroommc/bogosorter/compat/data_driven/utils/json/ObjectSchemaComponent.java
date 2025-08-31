package com.cleanroommc.bogosorter.compat.data_driven.utils.json;

import com.github.bsideup.jabel.Desugar;

/**
 * @author ZZZank
 */
@Desugar
public record ObjectSchemaComponent<T>(
    JsonSchema<T> schema,
    String id,
    boolean optional,
    T fallback
) {
}
