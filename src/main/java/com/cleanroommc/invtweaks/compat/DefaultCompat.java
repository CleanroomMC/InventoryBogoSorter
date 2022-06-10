package com.cleanroommc.invtweaks.compat;

import com.cleanroommc.invtweaks.api.InventoryTweaksAPI;
import net.minecraft.inventory.*;

public class DefaultCompat {

    public static void init() {
        InventoryTweaksAPI.addCompat(ContainerChest.class, (container, builder) -> {
            IInventory inventory = container.getLowerChestInventory();
            builder.addSlotGroup(9, 0, inventory.getSizeInventory());
        });
        InventoryTweaksAPI.addCompat(ContainerDispenser.class, (container, builder) -> {
            builder.addSlotGroup(3, 0, 9);
        });
        InventoryTweaksAPI.addCompat(ContainerHopper.class, (container, builder) -> {
            builder.addSlotGroup(5, 0, 5);
        });
        InventoryTweaksAPI.addCompat(ContainerShulkerBox.class, (container, builder) -> {
            builder.addSlotGroup(9, 0, 27);
        });
        // for horse inventory compat see MixinContainerHorseInventory
    }
}
