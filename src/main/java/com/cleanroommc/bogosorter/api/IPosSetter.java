package com.cleanroommc.bogosorter.api;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

public interface IPosSetter {

    IPosSetter DEFAULT = (gui, slotGroup, buttonPos) -> {
        if (slotGroup.getSlots().size() < slotGroup.getRowSize()) {
            buttonPos.setPos(-1000, -1000);
        } else {
            Slot topRight = slotGroup.getSlots().get(slotGroup.getRowSize() - 1);
            buttonPos.setPos(topRight.xPos + 17, topRight.yPos - 2);
        }
    };

    void setButtonPos(GuiContainer gui, ISlotGroup slotGroup, IButtonPos buttonPos);
}
