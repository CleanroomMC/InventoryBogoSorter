package com.cleanroommc.bogosorter.compat;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @author ZZZank
 */
public class FixedLimitSlot extends SlotDelegate {
    private final int sizeLimit;

    public FixedLimitSlot(Slot slot, int sizeLimit) {
        super(slot);
        if (sizeLimit <= 0) {
            throw new IllegalArgumentException(String.format(
                "size limit '%s' not valid, must be positive number",
                sizeLimit
            ));
        }
        this.sizeLimit = sizeLimit;
    }

    @Override
    public int bogo$getItemStackLimit(ItemStack itemStack) {
        return sizeLimit;
    }

    @Override
    public int bogo$getMaxStackSize(ItemStack itemStack) {
        return sizeLimit;
    }
}
