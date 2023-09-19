package com.cleanroommc.bogosorter.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * A custom slot interface. Useful if mods have a slot that does not implement the necessary methods.
 * {@link Slot} implements this interface via mixin.
 */
public interface ISlot {

    Slot getRealSlot();

    int getX();

    int getY();

    int getSlotNumber();

    int getSlotIndex();

    IInventory getInventory();

    void putStack(ItemStack itemStack);

    ItemStack getStack();

    int getMaxStackSize(ItemStack itemStack);

    int getItemStackLimit(ItemStack itemStack);

    boolean isEnabled();

    boolean isItemValid(ItemStack stack);

    boolean canTakeStack(EntityPlayer player);
}
