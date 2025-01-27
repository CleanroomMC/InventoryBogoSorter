package com.cleanroommc.bogosorter.core.mixin.hbm;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.hbm.inventory.container.ContainerCrateTungsten;
import com.hbm.tileentity.machine.TileEntityCrateTungsten;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ContainerCrateTungsten.class, remap = false)
public class MixinContainerCrateTungsten implements ISortableContainer {
    @Shadow
    private TileEntityCrateTungsten crate;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, crate.inventory.getSlots(), 8);
    }
}
