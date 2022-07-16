package com.cleanroommc.bogosorter.mixin.gtceu;

import com.cleanroommc.bogosorter.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import gregtech.api.items.IToolItem;
import gregtech.api.items.toolitem.ToolMetaItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ToolMetaItem.class)
public abstract class MixinToolMetaItem
{
    @Shadow
    public abstract int getItemDamage(ItemStack itemStack);

    @Shadow
    public abstract int getMaxItemDamage(ItemStack itemStack);

    @Inject(method = "damageItem", at = @At("RETURN"), remap = false)
    private void damageItem(ItemStack stack, EntityLivingBase entity, int vanillaDamage, boolean allowPartial, boolean simulate, CallbackInfoReturnable<Integer> cir){
        if (simulate) return;
        if (!(entity instanceof EntityPlayer)) return;

        EntityPlayer player = ((EntityPlayer) entity);

        if (RefillHandler.shouldHandleRefill(player, stack)) {
            int durabilityLeft = getMaxItemDamage(stack) - getItemDamage(stack);
            if (durabilityLeft >= 0 && durabilityLeft < BogoSorterConfig.autoRefillDamageThreshold) {
                new RefillHandler(player.inventory.currentItem, stack, player, true).handleRefill();
            }
        }
    }

}
