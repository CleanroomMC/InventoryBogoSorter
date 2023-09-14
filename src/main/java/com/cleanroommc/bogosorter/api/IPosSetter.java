package com.cleanroommc.bogosorter.api;

import net.minecraft.client.gui.inventory.GuiContainer;

public interface IPosSetter {

    void setButtonPos(GuiContainer gui, ISlotGroup slotGroup, IButtonPos buttonPos);
}
