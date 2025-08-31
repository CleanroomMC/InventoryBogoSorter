package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.ObjectJsonSchema;
import com.google.common.base.Preconditions;
import net.minecraft.inventory.Container;

import java.util.Optional;

/**
 * @author ZZZank
 */
public class RangedSlotHandler extends HandlerBase {
    public static final JsonSchema<RangedSlotHandler> SCHEMA = ObjectJsonSchema.of(
        BogoCondition.SCHEMA.toOptionalField("condition"),
        TARGET_SCHEMA.toField("target"),
        JsonSchema.INT.toField("start"),
        JsonSchema.INT.toField("end"),
        JsonSchema.INT.toField("rowSize"),
        RangedSlotHandler::new
    );

    protected RangedSlotHandler(
        Optional<BogoCondition> condition,
        Class<? extends Container> target,
        int start,
        int end,
        int rowSize
    ) {
        super(condition, target);
        Preconditions.checkArgument(start >= 0, "'start' must be no smaller than 0");
        Preconditions.checkArgument(end >= start, "'end' must be no smaller than 'start'");
        Preconditions.checkArgument(rowSize >= 0, "'row_size' must be no smaller than 0");
        this.start = start;
        this.end = end;
        this.rowSize = rowSize;
    }

    private final int start;
    private final int end;
    private final int rowSize;

    @Override
    protected void handleImpl(IBogoSortAPI api) {
        api.addCompat(target(), (container, builder) -> builder.addSlotGroup(start, end, rowSize));
    }
}
