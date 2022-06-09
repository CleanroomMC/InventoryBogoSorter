package com.cleanroommc.invtweaks;

import com.cleanroommc.invtweaks.api.ISortableContainer;
import com.cleanroommc.invtweaks.network.CSlotPosUpdate;
import com.cleanroommc.invtweaks.network.CSort;
import com.cleanroommc.invtweaks.network.NetworkHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;

@Mod(modid = InventoryTweaks.ID, name = InventoryTweaks.NAME, version = InventoryTweaks.VERSION)
@Mod.EventBusSubscriber(modid = InventoryTweaks.ID)
public class InventoryTweaks {

    public static final String ID = "inventorytweaks";
    public static final String NAME = "Brachy's Inventory Tweaks";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        NetworkHandler.init();
    }

    @SubscribeEvent
    public static void onGui(GuiScreenEvent.InitGuiEvent.Post event) {
        LOGGER.info("Open GUI");
        if (isSortableContainer(event.getGui())) {
            LOGGER.info(" - is sortable");
            ISortableContainer sortableContainer = getSortableContainer(event.getGui());
            Container container = getSortableContainer(event.getGui());
            NetworkHandler.sendToServer(new CSlotPosUpdate(container));
        }
    }

    @SubscribeEvent
    public static void onMouseInput(GuiScreenEvent.MouseInputEvent.Post event) {
        if (Mouse.getEventButton() == 2 && isSortableContainer(event.getGui())) {
            Slot slot = ((GuiContainer) event.getGui()).getSlotUnderMouse();
            LOGGER.info("About to sort. Slot: {}", slot == null ? "null" : slot.slotNumber);
            if (slot == null) return;
            NetworkHandler.sendToServer(new CSort(slot));
        }
    }

    public static boolean isSortableContainer(GuiScreen screen) {
        return screen instanceof GuiContainer && ((GuiContainer) screen).inventorySlots instanceof ISortableContainer;
    }

    public static <T extends Container & ISortableContainer> T getSortableContainer(GuiScreen screen) {
        return (T) ((GuiContainer) screen).inventorySlots;
    }

    public static String getMod(ItemStack stack) {
        return stack.getItem().getRegistryName().getNamespace();
    }

    public static String getId(ItemStack stack) {
        return stack.getItem().getRegistryName().getPath();
    }
}
