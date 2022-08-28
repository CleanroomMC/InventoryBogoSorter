package com.cleanroommc.bogosorter.compat;

import appeng.container.implementations.ContainerSkyChest;
import blusunrize.immersiveengineering.common.gui.ContainerCrate;
import codechicken.enderstorage.container.ContainerEnderItemStorage;
import com.brandon3055.draconicevolution.inventory.ContainerDraconiumChest;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerGiantChest;
import forestry.storage.gui.ContainerBackpack;
import net.minecraft.inventory.*;
import net.minecraftforge.fml.common.Loader;
import thedarkcolour.futuremc.container.ContainerBarrel;

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

        if (Loader.isModLoaded("actuallyadditions")) {
            api.addCompat(ContainerGiantChest.class, (container, builder) -> {
                builder.addSlotGroup(13, 0, 117);
            });
        }

        if (Loader.isModLoaded("enderstorage")) {
            api.addCompat(ContainerEnderItemStorage.class, (container, builder) -> {
                switch (container.chestInv.getSize()) {
                    case 0:
                        builder.addSlotGroup(3, 0, 9);
                        break;
                    case 1:
                        builder.addSlotGroup(9, 0, 27);
                        break;
                    case 2:
                        builder.addSlotGroup(9, 0, 54);
                        break;
                }
            });
        }

        if (Loader.isModLoaded("appliedenergistics2")) {
            api.addCompat(ContainerSkyChest.class, (container, builder) -> {
                builder.addSlotGroup(9, 0, 36);
            });
        }

        if (Loader.isModLoaded("draconicevolution")) {
            api.addCompatSimple(ContainerDraconiumChest.class, (container, builder) -> {
                builder.addSlotGroup(26, 0, 260);
            });
        }

        if (Loader.isModLoaded("futuremc")) {
            api.addCompatSimple(ContainerBarrel.class, (container, builder) -> {
                builder.addSlotGroup(9, 0, 27);
            });
        }

        if (Loader.isModLoaded("immersiveengineering")) {
            api.addCompat(ContainerCrate.class, (container, builder) -> {
                builder.addSlotGroup(9, 0, container.slotCount);
            });
        }

        if (Loader.isModLoaded("forestry")) {
            api.addCompat(ContainerBackpack.class, (container, builder) -> {
                if (container.inventorySlots.size() == 51) {
                    builder.addSlotGroup(5, 36, container.inventorySlots.size());
                } else {
                    builder.addSlotGroup(9, 36, container.inventorySlots.size());

                }
            });
        }
    }
}
