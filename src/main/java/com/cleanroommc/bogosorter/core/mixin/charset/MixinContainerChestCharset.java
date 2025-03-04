package com.cleanroommc.bogosorter.core.mixin.charset;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import pl.asie.charset.module.storage.chests.ContainerChestCharset;
import pl.asie.charset.module.storage.chests.TileEntityChestCharset;

@Mixin(value = ContainerChestCharset.class, remap = false)
public class MixinContainerChestCharset implements ISortableContainer {

    @Shadow
    @Final
    protected TileEntityChestCharset tile;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, tile.getSlots(), 9);
    }

}
