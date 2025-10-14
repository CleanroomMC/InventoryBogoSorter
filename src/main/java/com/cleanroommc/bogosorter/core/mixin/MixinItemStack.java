package com.cleanroommc.bogosorter.core.mixin;

import com.cleanroommc.bogosorter.common.refill.DamageHelper;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Inject(method = "attemptDamageItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setItemDamage(I)V", shift = At.Shift.AFTER), cancellable = true)
    private void damageItem(int amount, Random rand, EntityPlayerMP player, CallbackInfoReturnable<Boolean> cir) {
        if (player != null) {
            DamageHelper.damageItemHook(player, (ItemStack) (Object) this);
        }
    }
}
