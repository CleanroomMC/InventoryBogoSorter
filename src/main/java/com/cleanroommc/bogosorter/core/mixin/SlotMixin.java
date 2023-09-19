package com.cleanroommc.bogosorter.core.mixin;

import com.cleanroommc.bogosorter.api.ISlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public abstract class SlotMixin implements ISlot {

    @Shadow public int xPos;

    @Shadow public int yPos;

    @Shadow public int slotNumber;

    @Shadow @Final public IInventory inventory;

    @Override
    public Slot getRealSlot() {
        return (Slot) (Object) this;
    }

    @Override
    public int getX() {
        return xPos;
    }

    @Override
    public int getY() {
        return yPos;
    }

    @Override
    public int getSlotNumber() {
        return slotNumber;
    }

    @Override
    public IInventory getInventory() {
        return inventory;
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack) {
        return itemStack.getMaxStackSize();
    }
}
