package com.cleanroommc.bogosorter.core.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(GuiScreen.class)
public interface GuiScreenAccessor {

    @Accessor
    List<GuiButton> getButtonList();
}
