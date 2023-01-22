package com.cleanroommc.bogosorter.compat;

import appeng.container.implementations.ContainerSkyChest;
import blusunrize.immersiveengineering.common.gui.ContainerCrate;
import codechicken.enderstorage.container.ContainerEnderItemStorage;
import com.brandon3055.draconicevolution.inventory.ContainerDraconiumChest;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.gtce.IModularSortable;
import com.cleanroommc.bogosorter.compat.gtce.SortableSlotWidget;
import com.cleanroommc.bogosorter.core.mixin.colossalchests.ContainerColossalChestAccessor;
import com.lothrazar.cyclicmagic.item.storagesack.ContainerStorage;
import com.tiviacz.travelersbackpack.gui.container.ContainerTravelersBackpack;
import com.zuxelus.energycontrol.containers.ContainerCardHolder;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerGiantChest;
import forestry.core.gui.ContainerNaturalistInventory;
import forestry.storage.gui.ContainerBackpack;
import forestry.storage.gui.ContainerNaturalistBackpack;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.ModularUIContainer;
import ic2.core.block.personal.container.ContainerPersonalChest;
import ic2.core.block.storage.box.*;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.inventory.slots.SlotGhoest;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import jds.bibliocraft.containers.ContainerFramedChest;
import micdoodle8.mods.galacticraft.core.inventory.ContainerParaChest;
import mods.railcraft.common.gui.containers.ContainerRCChest;
import moze_intel.projecte.gameObjs.container.CondenserContainer;
import moze_intel.projecte.gameObjs.container.CondenserMK2Container;
import net.dries007.tfc.objects.container.ContainerChestTFC;
import net.minecraft.inventory.*;
import net.minecraftforge.fml.common.Loader;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChest;
import rustic.common.tileentity.ContainerCabinet;
import rustic.common.tileentity.ContainerCabinetDouble;
import t145.metalchests.containers.ContainerMetalChest;
import thebetweenlands.common.inventory.container.ContainerPouch;
import thedarkcolour.futuremc.container.ContainerBarrel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        if (Loader.isModLoaded("projecte")) {
            api.addCompat(CondenserContainer.class, (container, builder) -> {
                builder.addSlotGroup(13, 1, 92);
            });
            api.addCompat(CondenserMK2Container.class, (container, builder) -> {
                builder.addSlotGroup(6, 1, 43);
                builder.addSlotGroup(6, 43, 85);
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
            api.addCompat(ContainerNaturalistBackpack.class, (container, builder) -> {
                Slot[][] slotGroup = new Slot[25][5];
                for (int i = 0; i < 25; i++) {
                    for (int j = 0; j < 5; j++) {
                        slotGroup[i][j] = container.getSlot(i * 5 + j + 36);
                    }
                }
                builder.addSlotGroup(slotGroup);
            });
            api.addCompat(ContainerNaturalistInventory.class, (container, builder) -> {
                Slot[][] slotGroup = new Slot[25][5];
                for (int i = 0; i < 25; i++) {
                    for (int j = 0; j < 5; j++) {
                        slotGroup[i][j] = container.getSlot(i * 5 + j + 36);
                    }
                }
                builder.addSlotGroup(slotGroup);
            });
        }

        if (BogoSorter.isIc2ExpLoaded()) {
            api.addCompat(DynamicContainer.class, (container, builder) -> {
                if (container.base instanceof TileEntityStorageBox) {
                    if (container.base instanceof TileEntityWoodenStorageBox) {
                        builder.addSlotGroup(9, 0, 27);
                    } else if (container.base instanceof TileEntityBronzeStorageBox || container.base instanceof TileEntityIronStorageBox) {
                        builder.addSlotGroup(9, 0, 45);
                    } else if (container.base instanceof TileEntitySteelStorageBox) {
                        builder.addSlotGroup(9, 0, 63);
                    } else if (container.base instanceof TileEntityIridiumStorageBox) {
                        builder.addSlotGroup(18, 0, 126);
                    }
                }
            });
        }

        if (BogoSorter.isIc2ClassicLoaded()) {
            api.addCompat(ContainerPersonalChest.class, (container, builder) -> {
                // make sure player can edit this chest
                if (!(container.inventorySlots.get(0) instanceof SlotGhoest)) {
                    builder.addSlotGroup(9, 0, 54);
                }
            });
        }

        if (Loader.isModLoaded("metalchests")) {
            api.addCompat(ContainerMetalChest.class, (container, builder) -> {
                builder.addSlotGroup(container.type.getColumns(), 0, container.type.getInventorySize());
            });
        }

        if (BogoSorter.isGTCEuLoaded()) {
            api.addCompat(ModularUIContainer.class, (container, builder) -> {
                Map<String, List<Slot>> sortableSlots = new Object2ObjectOpenHashMap<>();

                for (Widget widget : container.getModularUI().getFlatVisibleWidgetCollection()) {
                    if (widget instanceof SortableSlotWidget) {
                        SortableSlotWidget sortableSlotWidget = (SortableSlotWidget) widget;
                        if (sortableSlotWidget.getSortArea() != null) {
                            sortableSlots.computeIfAbsent(sortableSlotWidget.getSortArea(), (key) -> new ArrayList<>()).add(sortableSlotWidget.getHandle());
                        }
                    }
                }

                for (Map.Entry<String, List<Slot>> entry : sortableSlots.entrySet()) {
                    int rowSize = ((IModularSortable) (Object) container.getModularUI()).getRowSize(entry.getKey());
                    if (rowSize > 0) {
                        builder.addSlotGroup(rowSize, entry.getValue());
                    }
                }
            });
        }

        if (Loader.isModLoaded("travelersbackpack")) {
            api.addCompat(ContainerTravelersBackpack.class, (container, builder) -> {
                Slot[][] slotGroup = new Slot[6][];
                for (int i = 0; i < 3; i++) {
                    Slot[] slotRow = new Slot[8];
                    slotGroup[i] = slotRow;
                    for (int j = 0; j < 8; j++) {
                        slotRow[j] = container.getSlot(i * 8 + j + 10);
                    }
                }
                for (int i = 3; i < 6; i++) {
                    Slot[] slotRow = new Slot[5];
                    slotGroup[i] = slotRow;
                    for (int j = 0; j < 5; j++) {
                        slotRow[j] = container.getSlot((i - 3) * 5 + j + 34);
                    }
                }
                builder.addSlotGroup(slotGroup);
            });
        }

        if (Loader.isModLoaded("colossalchests")) {
            api.addCompat(ContainerColossalChest.class, (container, builder) -> {
                List<Slot> chestSlots = ((ContainerColossalChestAccessor) container).getChestSlots();
                int size = chestSlots.size();
                int rows = size / 9;
                Slot[][] slotGroup = new Slot[rows][9];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < 9; j++) {
                        slotGroup[i][j] = chestSlots.get(i * 9 + j);
                    }
                }
                builder.addSlotGroup(slotGroup);
            });
            api.addCompat(ContainerUncolossalChest.class, (container, builder) -> {
                builder.addSlotGroup(5, 0, 5);
            });
        }

        if (Loader.isModLoaded("quark")) {
            api.addCompat(vazkii.quark.oddities.inventory.ContainerBackpack.class, (container, builder) -> {
                builder.addSlotGroup(9, 46, 46 + 27);
            });
        }

        if (Loader.isModLoaded("cyclicmagic")) {
            api.addCompat(ContainerStorage.class, (container, builder) -> {
                builder.addSlotGroup(11, 0, 77);
            });
        }

        if (Loader.isModLoaded("bibliocraft")) {
            api.addCompat(ContainerFramedChest.class, (container, builder) -> {
                builder.addSlotGroup(9, 0, container.getMainTile().getIsDouble() ? 27 * 2 : 27);
            });
        }

        if (Loader.isModLoaded("railcraft")) {
            api.addCompat(ContainerRCChest.class, (container, builder) -> {
                builder.addSlotGroup(9, 0, container.getInv().getSizeInventory());
            });
        }

        if (Loader.isModLoaded("energycontrol")) {
            api.addCompat(ContainerCardHolder.class, (container, builder) -> {
                builder.addSlotGroup(9, 0, 54);
            });
        }

        if (Loader.isModLoaded("projectred-exploration")) {
            api.addCompat(mrtjp.projectred.exploration.ContainerBackpack.class, (container, builder) -> {
                builder.addSlotGroup(9, 0, 27);
            });
        }

        if (Loader.isModLoaded("thebetweenlands")) {
            api.addCompat(ContainerPouch.class, (container, builder) -> {
                IInventory inventory = container.getItemInventory();
                builder.addSlotGroup(9, 0, inventory.getSizeInventory());
            });
        }

        if (Loader.isModLoaded("tfc")) {
            api.addCompat(ContainerChestTFC.class, (container, builder) -> {
                builder.addSlotGroup(9, 0, container.getLowerChestInventory().getSizeInventory());
            });
        }

        if (Loader.isModLoaded("galacticraftcore")) {
            api.addCompat(ContainerParaChest.class, (container, builder) -> {
                int slot = container.getparachestInventory().getSizeInventory() - 3;

                if (slot > 0) {
                    builder.addSlotGroup(9, 0, slot);
                }
            });
        }

        if (Loader.isModLoaded("rustic")) {
            api.addCompat(ContainerCabinet.class, (container, builder) -> {
                builder.addSlotGroup(9, 0, 27);
            });
            api.addCompat(ContainerCabinetDouble.class, (container, builder) -> {
                builder.addSlotGroup(9, 0, 54);
            });
        }
    }
}
