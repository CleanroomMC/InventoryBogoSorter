package com.cleanroommc.bogosorter.core.mixin;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.config.PlayerConfig;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ISpecialArmor;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow
    @Final
    private Item item;

    @Inject(method = "attemptDamageItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setItemDamage(I)V", shift = At.Shift.AFTER))
    private void damageItem(int amount, Random rand, EntityPlayerMP player, CallbackInfoReturnable<Boolean> cir) {
        if (player == null) return;
        PlayerConfig playerConfig = PlayerConfig.get(player);
        if (!playerConfig.enableAutoRefill || playerConfig.autoRefillDamageThreshold <= 0) return;

        ItemStack itemStack = bogosorter$getThis();
        if (RefillHandler.shouldHandleRefill(player, itemStack) && isNotArmor(itemStack)) {

            ItemStack handItem = player.getHeldItemMainhand();
            if (handItem != itemStack) {
                handItem = player.getHeldItemOffhand();
                if (handItem != itemStack) {
                    BogoSorter.LOGGER.info("Broken item was not found in player hand!");
                    return;
                }
            }

            int durabilityLeft = getMaxDamage() - getItemDamage();
            if (durabilityLeft >= 0 && durabilityLeft < playerConfig.autoRefillDamageThreshold) {
                RefillHandler.handle(player.inventory.currentItem, bogosorter$getThis(), player, true);
            }
        }
    }

    private static boolean isNotArmor(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemArmor || itemStack.getItem() instanceof ISpecialArmor) return false;
        EntityEquipmentSlot slot = itemStack.getItem().getEquipmentSlot(itemStack);
        return slot == null || slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND;
    }

    private ItemStack bogosorter$getThis() {
        return (ItemStack) (Object) this;
    }
}
