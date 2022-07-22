package com.cleanroommc.bogosorter.mixin;

import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.config.PlayerConfig;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract int getItemDamage();

    @Shadow
    public abstract int getMaxDamage();

    @Inject(method = "attemptDamageItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setItemDamage(I)V", shift = At.Shift.AFTER))
    private void damageItem(int amount, Random rand, EntityPlayerMP player, CallbackInfoReturnable<Boolean> cir) {
        PlayerConfig playerConfig = PlayerConfig.get(player);
        if (!playerConfig.enableAutoRefill || playerConfig.autoRefillDamageThreshold <= 0) return;

        if (RefillHandler.shouldHandleRefill(player, getThis())) {
            int durabilityLeft = getMaxDamage() - getItemDamage();
            if (durabilityLeft >= 0 && durabilityLeft < playerConfig.autoRefillDamageThreshold) {
                new RefillHandler(player.inventory.currentItem, getThis(), player, true).handleRefill();
            }
        }
    }

    private ItemStack getThis() {
        return (ItemStack) (Object) this;
    }
}
