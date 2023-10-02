package com.cleanroommc.bogosorter.core.mixin.improvedbackpacks;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.poopycoders.improvedbackpacks.inventory.InventoryBackpack;
import ru.poopycoders.improvedbackpacks.inventory.containers.ContainerBackpack;
import ru.poopycoders.improvedbackpacks.inventory.containers.ContainerBlockBackpack;
import ru.poopycoders.improvedbackpacks.tiles.TileEntityBackpack;

@Mixin(ContainerBlockBackpack.class)
public class ContainerBlockBackpackMixin implements ISortableContainer {


    @Shadow @Final private TileEntityBackpack tileEntityBackpack;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, tileEntityBackpack.getSizeInventory(), 9);
    }
}
