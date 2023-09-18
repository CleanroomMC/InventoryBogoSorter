package com.cleanroommc.bogosorter.api;

import net.minecraft.inventory.Slot;

/**
 * A function to set the sort button pos of {@link ISlotGroup}'s.
 */
@FunctionalInterface
public interface IPosSetter {

    IPosSetter TOP_RIGHT_HORIZONTAL = (slotGroup, buttonPos) -> {
        if (slotGroup.getSlots().size() < slotGroup.getRowSize()) {
            buttonPos.setPos(-1000, -1000);
        } else {
            Slot topRight = slotGroup.getSlots().get(slotGroup.getRowSize() - 1);
            buttonPos.setPos(topRight.xPos + 17, topRight.yPos - 2);
        }
    };

    IPosSetter TOP_RIGHT_VERTICAL = (slotGroup, buttonPos) -> {
        if (slotGroup.getSlots().size() < slotGroup.getRowSize()) {
            buttonPos.setPos(-1000, -1000);
        } else {
            Slot topRight = slotGroup.getSlots().get(slotGroup.getRowSize() - 1);
            buttonPos.setVertical();
            buttonPos.setTopLeft();
            buttonPos.setPos(topRight.xPos + 18, topRight.yPos - 1);
        }
    };

    /**
     * Called every frame, to make sure the buttons are always in the right position.
     * Call setters on {@link IButtonPos} here.
     *
     * @param slotGroup slot group of the buttons
     * @param buttonPos the mutable button pos
     */
    void setButtonPos(ISlotGroup slotGroup, IButtonPos buttonPos);
}
