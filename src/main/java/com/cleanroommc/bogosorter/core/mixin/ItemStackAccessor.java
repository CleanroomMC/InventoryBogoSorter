package com.cleanroommc.bogosorter.core.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ItemStack.class, remap = false)
public interface ItemStackAccessor {

    @Accessor
    NBTTagCompound getCapNBT();
}
