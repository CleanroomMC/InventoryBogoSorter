package com.cleanroommc.bogosorter.compat.data_driven.handlers;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import net.minecraft.inventory.Slot;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author ZZZank
 */
public class MappedSlotCompatHandler extends CompatHandlerBase {
    private final int rowSize;
    private final Function<List<Slot>, List<ISlot>> mapper;

    public MappedSlotCompatHandler(String className, int rowSize, Function<List<Slot>, List<ISlot>> mapper) {
        super(className);
        BogoSorter.LOGGER.info("constructed mapped bogo compat handler targeting '{}' with row size '{}'", className, rowSize);
        this.rowSize = rowSize;
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(toClass(), (container, builder) -> additionalAction(
            builder.addSlotGroup(mapper.apply(container.inventorySlots), rowSize))
        );
    }
}
