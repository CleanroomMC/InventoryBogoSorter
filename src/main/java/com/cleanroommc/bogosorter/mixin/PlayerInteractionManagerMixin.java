package com.cleanroommc.bogosorter.mixin;

import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin {

    @Shadow private GameType gameType;

    @Shadow public abstract boolean isCreative();

    @Shadow public EntityPlayerMP player;

    /**
     * @reason bad
     * @author brachy84
     */
    @Overwrite
    public EnumActionResult processRightClick(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand) {
        if (gameType == GameType.SPECTATOR) {
            return EnumActionResult.PASS;
        } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
            return EnumActionResult.PASS;
        } else {
            EnumActionResult cancelResult = net.minecraftforge.common.ForgeHooks.onItemRightClick(player, hand);
            if (cancelResult != null) return cancelResult;
            int i = stack.getCount();
            int j = stack.getMetadata();
            ItemStack copyBeforeUse = stack.copy();
            ItemStack copyCopy = stack.copy();
            ActionResult<ItemStack> actionresult = stack.useItemRightClick(worldIn, player, hand);
            ItemStack itemstack = actionresult.getResult();

            if (itemstack == stack && itemstack.getCount() == i && itemstack.getMaxItemUseDuration() <= 0 && itemstack.getMetadata() == j) {
                return actionresult.getType();
            } else if (actionresult.getType() == EnumActionResult.FAIL && itemstack.getMaxItemUseDuration() > 0 && !player.isHandActive()) {
                return actionresult.getType();
            } else {
                player.setHeldItem(hand, itemstack);

                if (isCreative()) {
                    itemstack.setCount(i);

                    if (itemstack.isItemStackDamageable()) {
                        itemstack.setItemDamage(j);
                    }
                }

                if (itemstack.isEmpty()) {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, hand);
                    player.setHeldItem(hand, ItemStack.EMPTY);
                    RefillHandler.onDestroyItem(player, hand, copyCopy);
                }

                if (!player.isHandActive()) {
                    ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
                }

                return actionresult.getType();
            }
        }
    }

    /*@Inject(
            method = "processRightClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;setHeldItem(Lnet/minecraft/util/EnumHand;Lnet/minecraft/item/ItemStack;)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    public void processRightClick(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        RefillHandler.onDestroyItem(player, hand, stack);
    }*/
}
