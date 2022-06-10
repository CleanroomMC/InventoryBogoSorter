package com.cleanroommc.invtweaks.compat;

import com.cleanroommc.invtweaks.api.InventoryTweaksAPI;
import net.minecraft.inventory.ContainerChest;

public class DefaultCompat {

    public static void init() {
        InventoryTweaksAPI.addCompat(ContainerChest.class, (container, builder) -> {
            builder.addSlotGroup(9, 0, 27);
        });
    }
}
