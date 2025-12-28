package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.github.bsideup.jabel.Desugar;
import com.google.common.base.Preconditions;
import net.minecraft.inventory.Container;

/**
 * @author ZZZank
 */
@Desugar
record RangedSlotHandler(
    Class<? extends Container> target,
    int start,
    int end,
    int rowSize
) implements BogoCompatHandler {
    public static final JsonSchema<RangedSlotHandler> SCHEMA = JsonSchema.object(
        TARGET_SCHEMA.toField("target"),
        JsonSchema.INT.describe("index of the first slot (including)").toField("start"),
        JsonSchema.INT.describe("index of the end slot (excluding)").toField("end"),
        ROW_SIZE_SCHEMA.toField("row_size"),
        RangedSlotHandler::new
    ).describe("Register a slot group for slots with index in [start, end) range");

    RangedSlotHandler {
        Preconditions.checkArgument(start >= 0, "'start' must be no smaller than 0");
        Preconditions.checkArgument(end >= start, "'end' must be no smaller than 'start'");
        Preconditions.checkArgument(rowSize >= 0, "'row_size' must be no smaller than 0");
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(target, (container, builder) -> builder.addSlotGroup(start, end, rowSize));
    }
}
