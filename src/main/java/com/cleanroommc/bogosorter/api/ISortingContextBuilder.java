package com.cleanroommc.bogosorter.api;

import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Meant for use in {@link ISortableContainer#buildSortingContext(ISortingContextBuilder)}
 */
@ApiStatus.NonExtendable
public interface ISortingContextBuilder {

    ISlotGroup addSlotGroup(List<Slot> slots, int rowSize);

    ISlotGroup addSlotGroup(int startIndex, int endIndex, int rowSize);
}
