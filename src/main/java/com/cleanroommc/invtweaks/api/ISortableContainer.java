package com.cleanroommc.invtweaks.api;

/**
 * implement on {@link net.minecraft.inventory.Container}
 */
public interface ISortableContainer {

    void buildSortingContext(ISortingContextBuilder builder);
}
