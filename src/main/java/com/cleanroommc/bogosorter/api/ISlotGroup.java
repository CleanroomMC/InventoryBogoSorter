package com.cleanroommc.bogosorter.api;

import com.cleanroommc.bogosorter.BogoSortAPI;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface ISlotGroup {

    @UnmodifiableView
    List<Slot> getSlots();

    int getRowSize();

    int getPriority();

    default boolean hasSlot(int slotNumber) {
        for (Slot slot : getSlots()) {
            if (slot.slotNumber == slotNumber) return true;
        }
        return false;
    }

    default boolean isEmpty() {
        return getSlots().isEmpty();
    }

    default boolean isPlayerInventory() {
        return !getSlots().isEmpty() && BogoSortAPI.isPlayerSlot(getSlots().get(0));
    }
}
