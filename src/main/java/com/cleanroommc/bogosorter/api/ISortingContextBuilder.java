package com.cleanroommc.bogosorter.api;

import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Meant for use in {@link ISortableContainer#buildSortingContext(ISortingContextBuilder)}
 */
@ApiStatus.NonExtendable
public interface ISortingContextBuilder {

    ISortingContextBuilder addSlotGroup(SlotGroup slotGroup);

    ISortingContextBuilder addSlotGroup(int rowSize, int startIndex, int endIndex);

    ISortingContextBuilder addSlotGroup(int rowSize, List<Slot> slots);
}
