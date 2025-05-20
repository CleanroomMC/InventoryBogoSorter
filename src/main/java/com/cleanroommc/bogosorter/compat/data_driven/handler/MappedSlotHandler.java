package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
public class MappedSlotHandler extends HandlerBase {
    private final int rowSize;
    private final List<Predicate<Slot>> filters;
    private final Function<Slot, ISlot> reducer;

    public MappedSlotHandler(
        Class<? extends Container> target,
        int rowSize,
        List<Predicate<Slot>> filters,
        Function<Slot, ISlot> reducer
    ) {
        super(target);
        this.rowSize = rowSize;
        this.filters = filters;
        this.reducer = reducer;
        BogoSorter.LOGGER.info(
            "constructed mapped bogo compat handler targeting '{}' with row size '{}'",
            target.getName(),
            rowSize
        );
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(
            target,
            (container, builder) -> {
                var s = container.inventorySlots.stream();
                for (var filter : filters) {
                    s = s.filter(filter);
                }
                builder.addSlotGroup(s.map(reducer).collect(Collectors.toList()), rowSize);
            }
        );
    }
}
