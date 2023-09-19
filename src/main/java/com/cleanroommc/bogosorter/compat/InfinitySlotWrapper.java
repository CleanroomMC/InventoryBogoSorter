package com.cleanroommc.bogosorter.compat;

import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.core.mixin.avaritiaddons.InfinityMatchingAccessor;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import wanion.avaritiaddons.block.chest.infinity.InfinitySlot;

public class InfinitySlotWrapper extends Slot implements ISlot {

    public final InfinitySlot infinitySlot;

    public InfinitySlotWrapper(InfinitySlot infinitySlot) {
        super(infinitySlot.inventory, infinitySlot.getSlotIndex(), infinitySlot.xPos, infinitySlot.yPos);
        this.infinitySlot = infinitySlot;
        this.slotNumber = infinitySlot.slotNumber;
    }

    @Override
    public Slot getRealSlot() {
        return infinitySlot;
    }

    @Override
    public int getX() {
        return infinitySlot.xPos;
    }

    @Override
    public int getY() {
        return infinitySlot.yPos;
    }

    @Override
    public int getSlotNumber() {
        return infinitySlot.slotNumber;
    }

    @Override
    public IInventory getInventory() {
        return infinitySlot.inventory;
    }

    @Override
    public void putStack(@NotNull ItemStack itemStack) {
        ((InfinityMatchingAccessor) (Object) this.infinitySlot.getInfinityMatching()).invokeSetStack(itemStack, itemStack.getCount());
        onSlotChanged();
    }

    @Override
    public @NotNull ItemStack getStack() {
        return this.infinitySlot.getInfinityMatching().getStack();
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }
}
