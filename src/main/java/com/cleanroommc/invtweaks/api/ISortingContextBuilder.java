package com.cleanroommc.invtweaks.api;

import net.minecraft.inventory.Slot;

import java.util.List;

public interface ISortingContextBuilder {

    ISortingContextBuilder addSlotGroup(Slot[][] slotGroup);

    ISortingContextBuilder addSlotGroup(int rowSize, int startIndex, int endIndex);

    ISortingContextBuilder addSlotGroup(int rowSize, List<Slot> slots);
}
