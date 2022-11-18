package com.cleanroommc.bogosorter.core.mixin;

import com.cleanroommc.bogosorter.common.HotbarSwap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    public EntityPlayerSP player;

    @Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(I)V"))
    public void mouseInput(InventoryPlayer instance, int direction) {
        if (!HotbarSwap.doCancelHotbarSwap()) {
            player.inventory.changeCurrentItem(direction);
        }
    }
}
