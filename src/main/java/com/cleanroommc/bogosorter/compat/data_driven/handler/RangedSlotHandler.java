package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZZZank
 */
public class RangedSlotHandler extends HandlerBase {
    /// ```
    /// {
    ///     "type": "ranged",
    ///     "start": int,
    ///     "end": int,
    ///     "row_size": int
    /// }
    /// ```
    public static RangedSlotHandler read(@NotNull JsonObject o, String targetClassName) {
        int start = o.get("start").getAsNumber().intValue();
        int end = o.get("end").getAsNumber().intValue();
        int rowSize = o.get("row_size").getAsNumber().intValue();
        return new RangedSlotHandler(targetClassName, start, end, rowSize);
    }

    private final int start;
    private final int end;
    private final int rowSize;

    public RangedSlotHandler(String targetClassName, int start, int end, int rowSize) {
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
