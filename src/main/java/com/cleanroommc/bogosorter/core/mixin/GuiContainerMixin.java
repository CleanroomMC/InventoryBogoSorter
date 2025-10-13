package com.cleanroommc.bogosorter.core.mixin;

import com.cleanroommc.bogosorter.common.sort.IGuiContainerAccessor;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(GuiContainer.class)
public class GuiContainerMixin extends GuiScreen implements IGuiContainerAccessor {

    @Shadow
    protected int guiTop;

    @Shadow
    protected int guiLeft;

    @Override
    public List<GuiButton> getButtons() {
        return buttonList;
    }

    @Override
    public int getGuiTop() {
        return guiTop;
    }

    @Override
    public int getGuiLeft() {
        return guiLeft;
    }
}
