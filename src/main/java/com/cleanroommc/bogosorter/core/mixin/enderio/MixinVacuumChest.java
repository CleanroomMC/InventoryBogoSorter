package com.cleanroommc.bogosorter.core.mixin.enderio;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import crazypants.enderio.machines.machine.vacuum.chest.ContainerVacuumChest;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerVacuumChest.class)
public class MixinVacuumChest implements ISortableContainer {

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(1, 28, 9);
    }
}
