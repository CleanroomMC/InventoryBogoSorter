package com.cleanroommc.bogosorter.compat;

import com.cleanroommc.bogosorter.api.ISlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotDelegate implements ISlot {

    private final Slot slot;

    public SlotDelegate(Slot slot) {
        this.slot = slot;
    }

    @Override
    public Slot bogo$getRealSlot() {
        return slot;
    }

    @Override
    public int bogo$getX() {
        return slot.xPos;
    }

    @Override
    public int bogo$getY() {
        return slot.yPos;
    }

    @Override
    public int bogo$getSlotNumber() {
        return slot.slotNumber;
    }

    @Override
    public int bogo$getSlotIndex() {
        return slot.getSlotIndex();
    }

    @Override
    public IInventory bogo$getInventory() {
        return slot.inventory;
    }

    @Override
    public void bogo$putStack(ItemStack itemStack) {
        slot.putStack(itemStack);
    }

    @Override
    public ItemStack bogo$getStack() {
        return slot.getStack();
    }

    @Override
    public int bogo$getMaxStackSize(ItemStack itemStack) {
        return itemStack.getMaxStackSize();
    }

    @Override
    public int bogo$getItemStackLimit(ItemStack itemStack) {
        return slot.getItemStackLimit(itemStack);
    }

    @Override
    public boolean bogo$isEnabled() {
        return slot.isEnabled();
    }

    @Override
    public boolean bogo$isItemValid(ItemStack stack) {
        return slot.isItemValid(stack);
    }

    @Override
    public boolean bogo$canTakeStack(EntityPlayer player) {
        return slot.canTakeStack(player);
    }

    @Override
    public void bogo$onSlotChanged() {
        slot.onSlotChanged();
    }
}
