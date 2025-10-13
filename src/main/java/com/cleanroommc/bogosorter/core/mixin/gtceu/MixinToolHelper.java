package com.cleanroommc.bogosorter.core.mixin.gtceu;

import com.cleanroommc.bogosorter.common.refill.DamageHelper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import gregtech.api.items.toolitem.ToolHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToolHelper.class)
public class MixinToolHelper {

    @Inject(method = "damageItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setItemDamage(I)V", shift = At.Shift.AFTER), cancellable = true)
    private static void damageItem(ItemStack stack, EntityLivingBase entity, int damage, CallbackInfo ci) {
        if (entity instanceof EntityPlayerMP && DamageHelper.damageItemHook((EntityPlayerMP) entity, stack)) {
            ci.cancel();
        }
    }
}
