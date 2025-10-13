package com.cleanroommc.bogosorter.core.mixin.simplybackpacks;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import com.flanks255.simplybackpacks.gui.BackpackContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BackpackContainer.class, remap = false)
public class BackpackContainerMixin implements ISortableContainer {

    @Shadow
    private int slotcount;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, slotcount, slotcount == 18 ? 9 : 11);
    }
}
