package com.cleanroommc.bogosorter.core.mixin;

import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.tileentity.TileEntitySign;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiEditSign.class)
public class GuiEditSignMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(TileEntitySign teSign, CallbackInfo ci) {
        ((GuiEditSign) (Object) this).setFocused(true);
    }
}
