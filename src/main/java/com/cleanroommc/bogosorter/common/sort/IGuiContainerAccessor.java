package com.cleanroommc.bogosorter.common.sort;

import net.minecraft.client.gui.GuiButton;

import java.util.List;

public interface IGuiContainerAccessor {

    List<GuiButton> getButtons();

    int getGuiTop();

    int getGuiLeft();
}
