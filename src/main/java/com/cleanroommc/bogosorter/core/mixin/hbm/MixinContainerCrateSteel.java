package com.cleanroommc.bogosorter.core.mixin.hbm;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.hbm.inventory.container.ContainerCrateSteel;
import com.hbm.tileentity.machine.TileEntityCrateSteel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ContainerCrateSteel.class, remap = false)
public class MixinContainerCrateSteel implements ISortableContainer {
    @Shadow
    private TileEntityCrateSteel diFurnace;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, diFurnace.inventory.getSlots(), 9);
    }
}
