package com.cleanroommc.bogosorter.api;

import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;

/**
 * implement on {@link net.minecraft.inventory.Container}
 */
public interface ISortableContainer {

    int SORT_BUTTON_SIZE = 8;

    @ApiStatus.OverrideOnly
    void buildSortingContext(ISortingContextBuilder builder);

    default void adjustSortButtonPosition(Slot[][] slots, Point pos) {
        Slot topRight = slots[0][slots[0].length - 1];
        pos.x = topRight.xPos + 18 - SORT_BUTTON_SIZE;
        pos.y = topRight.yPos - SORT_BUTTON_SIZE - 2;
    }
}
