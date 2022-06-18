package com.cleanroommc.bogosorter.compat;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerGiantChest;
import net.minecraft.inventory.*;

public class DefaultCompat {

    public static void init(IBogoSortAPI api) {
        // vanilla
        api.addCompat(ContainerChest.class, (container, builder) -> {
            IInventory inventory = container.getLowerChestInventory();
            builder.addSlotGroup(9, 0, inventory.getSizeInventory());
        });
        api.addCompat(ContainerDispenser.class, (container, builder) -> {
            builder.addSlotGroup(3, 0, 9);
        });
        api.addCompat(ContainerHopper.class, (container, builder) -> {
            builder.addSlotGroup(5, 0, 5);
        });
        api.addCompat(ContainerShulkerBox.class, (container, builder) -> {
            builder.addSlotGroup(9, 0, 27);
        });
        // for horse inventory compat see MixinContainerHorseInventory

        api.addCompat(ContainerGiantChest.class, (container, builder) -> {
            builder.addSlotGroup(13, 0, 117);
        });
    }
}
