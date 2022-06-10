package com.cleanroommc.invtweaks.compat;

import com.cleanroommc.invtweaks.api.InventoryTweaksAPI;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;

public class DefaultCompat {

    public static void init() {
        InventoryTweaksAPI.addCompat(ContainerChest.class, (container, builder) -> {
            IInventory inventory = container.getLowerChestInventory();
            builder.addSlotGroup(9, 0, inventory.getSizeInventory());
        });
    }
}
