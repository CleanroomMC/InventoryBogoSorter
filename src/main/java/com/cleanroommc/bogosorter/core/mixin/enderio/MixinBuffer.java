package com.cleanroommc.bogosorter.core.mixin.enderio;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import crazypants.enderio.machines.machine.buffer.ContainerBuffer;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerBuffer.class)
public class MixinBuffer implements ISortableContainer {

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        if (((Container) (Object) this).inventorySlots.size() > 36) {
            builder.addSlotGroup(3, 0, 9);
        }
    }
}
