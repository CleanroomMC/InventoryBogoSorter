package com.cleanroommc.invtweaks.mixin;

import com.cleanroommc.invtweaks.InventoryTweaks;
import com.cleanroommc.invtweaks.api.ISortableContainer;
import com.cleanroommc.invtweaks.sort.GuiInventoryContext;
import net.minecraft.inventory.ContainerChest;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerChest.class)
public class ChestContainerMixin implements ISortableContainer {

    @Override
    public GuiInventoryContext buildInventoryContext(GuiInventoryContext.Builder builder) {
        InventoryTweaks.LOGGER.info("Building context...");
        return builder.addSlotGroup(9, 0, 27).build();
    }
}
