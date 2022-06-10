package com.cleanroommc.invtweaks.mixin;

import com.cleanroommc.invtweaks.api.ISortableContainer;
import com.cleanroommc.invtweaks.api.ISortingContextBuilder;
import cpw.mods.ironchest.common.blocks.chest.IronChestType;
import cpw.mods.ironchest.common.gui.chest.ContainerIronChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerIronChest.class)
public class MixinIronChestContainer implements ISortableContainer {

    @Shadow
    private IronChestType type;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(type.rowLength, 0, type.size);
    }
}
