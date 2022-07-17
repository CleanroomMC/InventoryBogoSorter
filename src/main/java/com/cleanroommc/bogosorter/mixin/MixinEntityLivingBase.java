package com.cleanroommc.bogosorter.mixin;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase
{
    /**
     *  activeItemStackCopy = what was used
     *  itemstack = the item's onItemUseFinish return item (usually empty, but can return a bowl, bottle, etc)
     */
    @Inject(method = "onItemUseFinish",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;setHeldItem(Lnet/minecraft/util/EnumHand;Lnet/minecraft/item/ItemStack;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onItemUseFinish(CallbackInfo ci, ItemStack activeItemStackCopy, ItemStack itemstack){
        if (!(getThis() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) getThis();

        //  used in cases where a modded item returns itself with a different durability (AA coffee, Botania Vials, etc)
        if (ItemStack.areItemsEqualIgnoreDurability(activeItemStackCopy, itemstack)){
            return;
        }

        if (RefillHandler.shouldHandleRefill(player, activeItemStackCopy)) {
            boolean didSwap = new RefillHandler(player.inventory.currentItem, activeItemStackCopy, player).handleRefill();
            if (didSwap && !itemstack.isEmpty()){
                if (!player.inventory.addItemStackToInventory(itemstack)) {
                    BogoSorter.LOGGER.info("Dropping item that does not fit");
                    player.dropItem(itemstack, true, false);
                }
            }
        }
    }

    private EntityLivingBase getThis() {
        return (EntityLivingBase) (Object) this;
    }
}
