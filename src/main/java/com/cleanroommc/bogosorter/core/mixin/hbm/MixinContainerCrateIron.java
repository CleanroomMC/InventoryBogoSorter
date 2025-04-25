package com.cleanroommc.bogosorter.core.mixin.hbm;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.hbm.inventory.container.ContainerCrateIron;
import com.hbm.tileentity.machine.TileEntityCrateIron;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ContainerCrateIron.class, remap = false)
public class MixinContainerCrateIron implements ISortableContainer {
    @Shadow
    private TileEntityCrateIron diFurnace;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, diFurnace.inventory.getSlots(), 9);
    }
}
