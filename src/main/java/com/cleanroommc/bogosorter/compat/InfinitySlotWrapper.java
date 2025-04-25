package com.cleanroommc.bogosorter.compat;

import com.cleanroommc.bogosorter.core.mixin.avaritiaddons.InfinityMatchingAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import wanion.avaritiaddons.block.chest.infinity.InfinitySlot;

public class InfinitySlotWrapper extends FixedLimitSlot {

    public final InfinitySlot infinitySlot;

    public InfinitySlotWrapper(InfinitySlot slot) {
        super(slot, Integer.MAX_VALUE);
        this.infinitySlot = slot;
    }

    @Override
    public void bogo$putStack(@NotNull ItemStack itemStack) {
        ((InfinityMatchingAccessor) (Object) this.infinitySlot.getInfinityMatching()).invokeSetStack(itemStack, itemStack.getCount());
        bogo$onSlotChanged();
    }

    @Override
    public @NotNull ItemStack bogo$getStack() {
        return this.infinitySlot.getInfinityMatching().getStack();
    }

    @Override
    public boolean bogo$isItemValid(ItemStack stack) {
        return true;
    }

    @Override
    public boolean bogo$canTakeStack(EntityPlayer player) {
        return true;
    }
}
