package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.inventory.Container;
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
    public static RangedSlotHandler read(@NotNull JsonObject o) {
        return new RangedSlotHandler(
            readClass(o),
            o.get("start").getAsInt(),
            o.get("end").getAsInt(),
            o.get("row_size").getAsInt()
        );
    }

    private final int start;
    private final int end;
    private final int rowSize;

    public RangedSlotHandler(Class<? extends Container> target, int start, int end, int rowSize) {
        super(target);
        Preconditions.checkArgument(start >= 0, "'start' must be no smaller than 0");
        Preconditions.checkArgument(end >= start, "'end' must be no smaller than 'start'");
        Preconditions.checkArgument(rowSize >= 0, "'row_size' must be no smaller than 0");
        this.start = start;
        this.end = end;
        this.rowSize = rowSize;
        BogoSorter.LOGGER.info(
            "constructed ranged bogo compat handler targeting '{}', with start {}, end {}, row size {}",
            target.getName(),
            start,
            end,
            rowSize
        );
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(target, (container, builder) -> builder.addSlotGroup(start, end, rowSize));
    }
}
