package com.cleanroommc.bogosorter.core.mixin.quark;

import com.cleanroommc.bogosorter.common.config.PlayerConfig;

import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.quark.management.feature.AutomaticToolRestock;

@Mixin(value = AutomaticToolRestock.class, remap = false)
public class AutomaticToolRestockMixin {

    @Inject(method = "onToolBreak", at = @At("HEAD"), cancellable = true)
    public void onToolBreak(PlayerDestroyItemEvent event, CallbackInfo ci) {
        if (PlayerConfig.get(event.getEntityPlayer()).enableAutoRefill) {
            ci.cancel();
        }
    }
}
