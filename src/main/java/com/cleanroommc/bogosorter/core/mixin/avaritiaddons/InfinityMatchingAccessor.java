package com.cleanroommc.bogosorter.core.mixin.avaritiaddons;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import wanion.avaritiaddons.block.chest.infinity.InfinityMatching;

@Mixin(value = InfinityMatching.class, remap = false)
public interface InfinityMatchingAccessor {

    @Invoker
    void invokeSetStack(ItemStack itemStack, int count);
}
