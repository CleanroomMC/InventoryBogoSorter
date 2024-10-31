package com.cleanroommc.bogosorter.compat.data_driven.handlers;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.google.common.base.Preconditions;

/**
 * @author ZZZank
 */
public class RangedSlotCompatHandler extends CompatHandlerBase {
    private final int start;
    private final int end;
    private final int rowSize;

    public RangedSlotCompatHandler(String targetClassName, int start, int end, int rowSize) {
        super(targetClassName);
        Preconditions.checkArgument(start >= 0, "'start' must be no smaller than 0");
        Preconditions.checkArgument(end >= start, "'end' must be no smaller than 'start'");
        Preconditions.checkArgument(rowSize >= 0, "'row_size' must be no smaller than 0");
        this.start = start;
        this.end = end;
        this.rowSize = rowSize;
        BogoSorter.LOGGER.info(
            "constructed ranged bogo compat handler targeting '{}', with start {}, end {}, row size {}",
            targetClassName,
            start,
            end,
            rowSize
        );
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(toClass(), (container, builder) -> additionalAction(builder.addSlotGroup(start, end, rowSize)));
    }
}
