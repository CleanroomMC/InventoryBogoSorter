package com.cleanroommc.bogosorter.core.mixin.hbm;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.hbm.inventory.container.ContainerCrateDesh;
import com.hbm.tileentity.machine.TileEntityCrateDesh;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ContainerCrateDesh.class, remap = false)
public class MixinContainerCrateDesh implements ISortableContainer {
    @Shadow
    private TileEntityCrateDesh crate;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, crate.inventory.getSlots(), 13);
    }
}
