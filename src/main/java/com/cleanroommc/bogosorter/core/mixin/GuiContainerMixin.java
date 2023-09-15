package com.cleanroommc.bogosorter.core.mixin;

import com.cleanroommc.bogosorter.common.sort.IGuiButtonAccessor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(GuiContainer.class)
public class GuiContainerMixin extends GuiScreen implements IGuiButtonAccessor {

    @Override
    public List<GuiButton> getButtons() {
        return buttonList;
    }
}
