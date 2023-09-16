package com.cleanroommc.bogosorter.core.mixin.enderio;

import crazypants.enderio.base.machine.baselegacy.AbstractInventoryMachineEntity;
import crazypants.enderio.base.machine.gui.AbstractMachineContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractMachineContainer.class)
public interface EIOContainerAccessor<E extends AbstractInventoryMachineEntity> {

    @Accessor
    E getTe();
}
