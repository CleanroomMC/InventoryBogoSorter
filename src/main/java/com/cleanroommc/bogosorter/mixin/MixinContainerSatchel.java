package com.cleanroommc.bogosorter.mixin;

import cofh.thermalexpansion.gui.container.storage.ContainerSatchel;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerSatchel.class)
public abstract class MixinContainerSatchel implements ISortableContainer {

    @Shadow
    int rowSize;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(rowSize, 36, ((Container) (Object) this).inventorySlots.size());
    }
}
