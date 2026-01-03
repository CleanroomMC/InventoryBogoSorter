package com.cleanroommc.bogosorter.core.mixin;

import com.cleanroommc.bogosorter.BogoSortAPI;

import com.cleanroommc.bogosorter.common.lock.LockSlotCapability;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Container.class)
public class ContainerMixin {

    @WrapOperation(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Slot;canTakeStack(Lnet/minecraft/entity/player/EntityPlayer;)Z"))
    public boolean injectBlockLockedSlots(Slot instance, EntityPlayer playerIn, Operation<Boolean> original) {
        return original.call(instance, playerIn) && (!BogoSortAPI.isPlayerSlot(instance) || !LockSlotCapability.getForPlayer(playerIn).isSlotLocked(instance.getSlotIndex()));
    }
}
