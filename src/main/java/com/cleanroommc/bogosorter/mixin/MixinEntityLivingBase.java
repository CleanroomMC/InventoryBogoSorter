package com.cleanroommc.bogosorter.mixin;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
     *  this.activeItemStack = not empty when there is an item in active slot. Used to detect when to refill
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

        if (RefillHandler.shouldHandleRefill(player, activeItemStackCopy)) {
            new RefillHandler(player.inventory.currentItem, activeItemStackCopy, player).handleRefill();
            if (!itemstack.isEmpty()){
                if (!player.inventory.addItemStackToInventory(itemstack)){
                    player.dropItem(itemstack, true);
                }
            }
        }
    }

    private EntityLivingBase getThis() {
        return (EntityLivingBase) (Object) this;
    }
}
