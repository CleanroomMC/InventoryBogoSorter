package com.cleanroommc.invtweaks.api;

import com.cleanroommc.invtweaks.sort.GuiInventoryContext;

/**
 * implement on {@link net.minecraft.inventory.Container}
 */
public interface ISortableContainer {

    GuiInventoryContext buildInventoryContext(GuiInventoryContext.Builder builder);
}
