package com.cleanroommc.bogosorter.api;

import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Meant for use in {@link ISortableContainer#buildSortingContext(ISortingContextBuilder)}
 */
@ApiStatus.NonExtendable
public interface ISortingContextBuilder {

    ISortingContextBuilder addSlotGroup(SlotGroup slotGroup);

    ISortingContextBuilder addSlotGroup(int rowSize, int startIndex, int endIndex);

    ISortingContextBuilder addSlotGroup(int rowSize, int startIndex, int endIndex, int priority);

    ISortingContextBuilder addSlotGroup(int rowSize, int startIndex, int endIndex, int priority, BiConsumer<SlotGroup, Point> pointSetter);

    ISortingContextBuilder addSlotGroup(int rowSize, List<Slot> slots);

    ISortingContextBuilder addSlotGroup(int rowSize, List<Slot> slots, int priority);

    ISortingContextBuilder addSlotGroup(int rowSize, List<Slot> slots, int priority, BiConsumer<SlotGroup, Point> posSetter);
}
