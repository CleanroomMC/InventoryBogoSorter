package com.cleanroommc.bogosorter.core.mixin.ironchests;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import cpw.mods.ironchest.common.blocks.shulker.IronShulkerBoxType;
import cpw.mods.ironchest.common.gui.shulker.ContainerIronShulkerBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ContainerIronShulkerBox.class, remap = false)
public class MixinIronShulkerContainer implements ISortableContainer {

    @Shadow
    private IronShulkerBoxType type;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, type.size, type.rowLength);
    }
}
