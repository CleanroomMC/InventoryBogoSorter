package com.cleanroommc.invtweaks.mixin;

import com.cleanroommc.invtweaks.InventoryTweaks;
import com.cleanroommc.invtweaks.api.ISortableContainer;
import com.cleanroommc.invtweaks.api.ISortingContextBuilder;
import net.minecraft.inventory.ContainerChest;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerChest.class)
public class ChestContainerMixin implements ISortableContainer {

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        InventoryTweaks.LOGGER.info("Building context...");
        builder.addSlotGroup(9, 0, 27);
    }
}
