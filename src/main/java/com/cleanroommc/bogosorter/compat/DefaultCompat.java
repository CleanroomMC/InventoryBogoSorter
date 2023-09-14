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
        api.addCompat(ContainerPlayer.class, (container, builder) -> {
            // player slots are automatically added
        });
        api.addCompat(ContainerChest.class, (container, builder) -> {
            IInventory inventory = container.getLowerChestInventory();
            builder.addSlotGroup(0, inventory.getSizeInventory(), 9);
        });
        api.addCompat(ContainerDispenser.class, (container, builder) -> {
            builder.addSlotGroup(0, 9, 3);
        });
        api.addCompat(ContainerHopper.class, (container, builder) -> {
            builder.addSlotGroup(0, 5, 5);
        });
        api.addCompat(ContainerShulkerBox.class, (container, builder) -> {
            builder.addSlotGroup(0, 27, 9);
        });
        // for horse inventory compat see MixinContainerHorseInventory

        if (Loader.isModLoaded("actuallyadditions")) {
            api.addCompat(ContainerGiantChest.class, (container, builder) -> {
                builder.addSlotGroup(0, 117, 13);
            });
        }

        if (Loader.isModLoaded("enderstorage")) {
            api.addCompat(ContainerEnderItemStorage.class, (container, builder) -> {
                switch (container.chestInv.getSize()) {
                    case 0:
                        builder.addSlotGroup(0, 9, 3);
                        break;
                    case 1:
                        builder.addSlotGroup(0, 27, 9);
                        break;
                    case 2:
                        builder.addSlotGroup(0, 54, 9);
                        break;
                }
            });
        }

        if (Loader.isModLoaded("appliedenergistics2")) {
            api.addCompat(ContainerSkyChest.class, (container, builder) -> {
                builder.addSlotGroup(0, 36, 9);
            });
        }

        if (Loader.isModLoaded("draconicevolution")) {
            api.addCompatSimple(ContainerDraconiumChest.class, (container, builder) -> {
                builder.addSlotGroup(0, 260, 26);
            });
        }

        if (Loader.isModLoaded("futuremc")) {
            api.addCompatSimple(ContainerBarrel.class, (container, builder) -> {
                builder.addSlotGroup(0, 27, 9);
            });
        }

        if (Loader.isModLoaded("projecte")) {
            api.addCompat(CondenserContainer.class, (container, builder) -> {
                builder.addSlotGroup(1, 92, 13);
            });
            api.addCompat(CondenserMK2Container.class, (container, builder) -> {
                builder.addSlotGroup(1, 43, 6);
                builder.addSlotGroup(43, 85, 6);
            });
        }

        if (Loader.isModLoaded("immersiveengineering")) {
            api.addCompat(ContainerCrate.class, (container, builder) -> {
                builder.addSlotGroup(0, container.slotCount, 9);
            });
        }

        if (Loader.isModLoaded("forestry")) {
            api.addCompat(ContainerBackpack.class, (container, builder) -> {
                if (container.inventorySlots.size() == 51) {
                    builder.addSlotGroup(36, container.inventorySlots.size(), 5);
                } else {
                    builder.addSlotGroup(36, container.inventorySlots.size(), 9);

                }
            });
            api.addCompat(ContainerNaturalistBackpack.class, (container, builder) -> {
                List<Slot> slots = new ArrayList<>();
                for (int i = 0; i < 25; i++) {
                    for (int j = 0; j < 5; j++) {
                        //slotGroup[i][j] = container.getSlot(i * 5 + j + 36);
                        slots.add(container.getSlot(i * 5 + j + 36));
                    }
                }
                builder.addSlotGroup(slots, 25);
            });
            api.addCompat(ContainerNaturalistInventory.class, (container, builder) -> {
                List<Slot> slots = new ArrayList<>();
                for (int i = 0; i < 25; i++) {
                    for (int j = 0; j < 5; j++) {
                        //slotGroup[i][j] = container.getSlot(i * 5 + j + 36);
                        slots.add(container.getSlot(i * 5 + j + 36));
                    }
                }
                builder.addSlotGroup(slots, 25);
            });
        }

        if (BogoSorter.isIc2ExpLoaded()) {
            api.addCompat(DynamicContainer.class, (container, builder) -> {
                if (container.base instanceof TileEntityStorageBox) {
                    if (container.base instanceof TileEntityWoodenStorageBox) {
                        builder.addSlotGroup(0, 27, 9);
                    } else if (container.base instanceof TileEntityBronzeStorageBox || container.base instanceof TileEntityIronStorageBox) {
                        builder.addSlotGroup(0, 45, 9);
                    } else if (container.base instanceof TileEntitySteelStorageBox) {
                        builder.addSlotGroup(0, 63, 9);
                    } else if (container.base instanceof TileEntityIridiumStorageBox) {
                        builder.addSlotGroup(0, 126, 18);
                    }
                }
            });
        }

        if (BogoSorter.isIc2ClassicLoaded()) {
            api.addCompat(ContainerPersonalChest.class, (container, builder) -> {
                // make sure player can edit this chest
                if (!(container.inventorySlots.get(0) instanceof SlotGhoest)) {
                    builder.addSlotGroup(0, 54, 9);
                }
            });
        }

        if (Loader.isModLoaded("metalchests")) {
            api.addCompat(ContainerMetalChest.class, (container, builder) -> {
                builder.addSlotGroup(0, container.type.getInventorySize(), container.type.getColumns());
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
                        builder.addSlotGroup(entry.getValue(), rowSize);
                    }
                }
            });
        }

        if (Loader.isModLoaded("travelersbackpack")) {
            api.addCompat(ContainerTravelersBackpack.class, (container, builder) -> {
                List<Slot> slots = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 8; j++) {
                        slots.add(container.getSlot(i * 8 + j + 10));
                    }
                }
                for (int i = 3; i < 6; i++) {
                    for (int j = 0; j < 5; j++) {
                        slots.add(container.getSlot((i - 3) * 5 + j + 34));
                    }
                }
                builder.addSlotGroup(slots, 8);
            });
        }

        if (Loader.isModLoaded("colossalchests")) {
            api.addCompat(ContainerColossalChest.class, (container, builder) -> {
                List<Slot> chestSlots = ((ContainerColossalChestAccessor) container).getChestSlots();
                builder.addSlotGroup(chestSlots, 9).buttonPosSetter((gui, slotGroup, buttonPos) -> {
                    buttonPos.setPos(0, 1000);
                    for (Slot slot : slotGroup.getSlots()) {
                        if (slot.xPos >= 0 && slot.yPos >= 0 && slot.isEnabled()) {
                            buttonPos.setPos(Math.max(buttonPos.getX(), slot.xPos + 17), Math.min(buttonPos.getY(), slot.yPos - 2));
                        }
                    }
                });
            });
            api.addPlayerSortButtonPosition(ContainerColossalChest.class, (gui, slotGroup, buttonPos) -> {
                Slot slot = slotGroup.getSlots().get(26);
                buttonPos.setPos(slot.xPos + 19, slot.yPos - 2);
                buttonPos.setTopLeft();
                buttonPos.setVertical();
            });
            api.addCompat(ContainerUncolossalChest.class, (container, builder) -> {
                builder.addSlotGroup(0, 5, 5);
            });
        }

        if (Loader.isModLoaded("quark")) {
            api.addCompat(vazkii.quark.oddities.inventory.ContainerBackpack.class, (container, builder) -> {
                builder.addSlotGroup(46, 46 + 27, 9);
            });
        }

        if (Loader.isModLoaded("cyclicmagic")) {
            api.addCompat(ContainerStorage.class, (container, builder) -> {
                builder.addSlotGroup(0, 77, 11);
            });
        }

        if (Loader.isModLoaded("bibliocraft")) {
            api.addCompat(ContainerFramedChest.class, (container, builder) -> {
                builder.addSlotGroup(0, container.getMainTile().getIsDouble() ? 27 * 2 : 27, 9);
            });
        }

        if (Loader.isModLoaded("railcraft")) {
            api.addCompat(ContainerRCChest.class, (container, builder) -> {
                builder.addSlotGroup(0, container.getInv().getSizeInventory(), 9);
            });
        }

        if (Loader.isModLoaded("energycontrol")) {
            api.addCompat(ContainerCardHolder.class, (container, builder) -> {
                builder.addSlotGroup(0, 54, 9);
            });
        }

        if (Loader.isModLoaded("projectred-exploration")) {
            api.addCompat(mrtjp.projectred.exploration.ContainerBackpack.class, (container, builder) -> {
                builder.addSlotGroup(0, 27, 9);
            });
        }

        if (Loader.isModLoaded("thebetweenlands")) {
            api.addCompat(ContainerPouch.class, (container, builder) -> {
                IInventory inventory = container.getItemInventory();
                builder.addSlotGroup(0, inventory.getSizeInventory(), 9);
            });
        }

        if (Loader.isModLoaded("tfc")) {
            api.addCompat(ContainerChestTFC.class, (container, builder) -> {
                builder.addSlotGroup(0, container.getLowerChestInventory().getSizeInventory(), 9);
            });
        }

        if (Loader.isModLoaded("galacticraftcore")) {
            api.addCompat(ContainerParaChest.class, (container, builder) -> {
                int slot = container.getparachestInventory().getSizeInventory() - 3;

                if (slot > 0) {
                    builder.addSlotGroup(0, slot, 9);
                }
            });
        }

        if (Loader.isModLoaded("rustic")) {
            api.addCompat(ContainerCabinet.class, (container, builder) -> {
                builder.addSlotGroup(0, 27, 9);
            });
            api.addCompat(ContainerCabinetDouble.class, (container, builder) -> {
                builder.addSlotGroup(0, 54, 9);
            });
        }
    }
}
