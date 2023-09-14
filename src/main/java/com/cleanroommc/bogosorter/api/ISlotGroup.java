package com.cleanroommc.bogosorter.api;

import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface ISlotGroup {

    @UnmodifiableView
    List<Slot> getSlots();

    int getRowSize();

    int getPriority();

    boolean isPlayerInventory();

    /**
     * Sets the priority of this slot group. Can determine where items are transferred first with shortcuts.
     *
     * @param priority priority
     * @return this
     */
    ISlotGroup priority(int priority);

    /**
     * Sets a custom function to determine the position of sort buttons. Default is top right corner.
     *
     * @param posSetter pos function or null if no buttons are desired
     * @return this
     */
    ISlotGroup buttonPosSetter(@Nullable IPosSetter posSetter);
}
