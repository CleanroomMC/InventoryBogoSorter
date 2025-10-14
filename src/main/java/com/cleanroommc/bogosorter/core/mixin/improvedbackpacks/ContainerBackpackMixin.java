package com.cleanroommc.bogosorter.core.mixin.improvedbackpacks;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.poopycoders.improvedbackpacks.inventory.InventoryBackpack;
import ru.poopycoders.improvedbackpacks.inventory.containers.ContainerBackpack;

@Mixin(value = ContainerBackpack.class, remap = false)
public class ContainerBackpackMixin implements ISortableContainer {

    @Shadow
    @Final
    private InventoryBackpack backpackInventory;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, backpackInventory.getSizeInventory(), 9);
    }
}
