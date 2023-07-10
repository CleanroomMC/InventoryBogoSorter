package com.cleanroommc.bogosorter.core.mixin.colossalchests;

import net.minecraft.inventory.Slot;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = ContainerColossalChest.class, remap = false)
public interface ContainerColossalChestAccessor {

    @Accessor
    List<Slot> getChestSlots();

}
