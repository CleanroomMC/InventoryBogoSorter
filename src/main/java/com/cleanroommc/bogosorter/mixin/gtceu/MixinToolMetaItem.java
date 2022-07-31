package com.cleanroommc.bogosorter.mixin.gtceu;

import com.cleanroommc.bogosorter.common.config.PlayerConfig;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import gregtech.api.items.toolitem.ToolMetaItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ToolMetaItem.class, remap = false)
public abstract class MixinToolMetaItem {

    @Shadow
    public abstract int getItemDamage(ItemStack itemStack);

    @Shadow
    public abstract int getMaxItemDamage(ItemStack itemStack);

    @Inject(method = "damageItem", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private void damageItem(ItemStack stack, EntityLivingBase entity, int vanillaDamage, boolean allowPartial, boolean simulate, CallbackInfoReturnable<Integer> cir) {
        if (!simulate && entity instanceof EntityPlayer) {
            EntityPlayer player = ((EntityPlayer) entity);
            PlayerConfig playerConfig = PlayerConfig.get(player);
            if (!playerConfig.enableAutoRefill || playerConfig.autoRefillDamageThreshold <= 0) return;

            if (RefillHandler.shouldHandleRefill(player, stack) && stack.hasTagCompound() && stack.getTagCompound().hasKey("GT.ToolStats")) {
                int durabilityLeft = getMaxItemDamage(stack) - getItemDamage(stack);
                if (durabilityLeft > 0 && durabilityLeft <= playerConfig.autoRefillDamageThreshold) {
                    new RefillHandler(player.inventory.currentItem, stack, player, true).handleRefill();
                }
            }
        }
    }
}
