package com.cleanroommc.bogosorter.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * implement on {@link net.minecraft.inventory.Container}
 */
public interface ISortableContainer {

    @ApiStatus.OverrideOnly
    void buildSortingContext(ISortingContextBuilder builder);

    @Nullable
    default IPosSetter getPlayerButtonPosSetter() {
        return IPosSetter.TOP_RIGHT_HORIZONTAL;
    }
}
