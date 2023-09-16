package com.cleanroommc.bogosorter.core.mixin.enderio;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import crazypants.enderio.machines.machine.buffer.ContainerBuffer;
import crazypants.enderio.machines.machine.buffer.TileBuffer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerBuffer.class)
public abstract class MixinBuffer implements ISortableContainer {

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        EIOContainerAccessor<? extends TileBuffer> eioContainer = (EIOContainerAccessor<? extends TileBuffer>) this;
        if (eioContainer.getTe() instanceof TileBuffer.TileBufferItem) {
            builder.addSlotGroup(0, 9, 3)
                    .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
        } else if (eioContainer.getTe() instanceof TileBuffer.TileBufferOmni) {
            builder.addSlotGroup(0, 9, 3);
        }
    }
}
