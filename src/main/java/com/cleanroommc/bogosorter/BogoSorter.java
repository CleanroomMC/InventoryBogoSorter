package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.InventoryTweaksAPI;
import com.cleanroommc.bogosorter.compat.DefaultCompat;
import com.cleanroommc.bogosorter.network.CSlotPosUpdate;
import com.cleanroommc.bogosorter.network.CSort;
import com.cleanroommc.bogosorter.network.NetworkHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
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

@Mod(modid = BogoSorter.ID, name = BogoSorter.NAME, version = BogoSorter.VERSION)
@Mod.EventBusSubscriber(modid = BogoSorter.ID)
public class BogoSorter {

    public static final String ID = "bogosorter";
    public static final String NAME = "Inventory Bogo Sorter";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        NetworkHandler.init();
        DefaultCompat.init();
    }

    @SubscribeEvent
    public static void onGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiContainer && !(event.getGui() instanceof GuiContainerCreative)) {
            Container container = ((GuiContainer) event.getGui()).inventorySlots;
            NetworkHandler.sendToServer(new CSlotPosUpdate(container));
        }
    }

    @SubscribeEvent
    public static void onMouseInput(GuiScreenEvent.MouseInputEvent.Post event) {
        if (Mouse.getEventButton() == 2 && event.getGui() instanceof GuiContainer) {
            Slot slot = ((GuiContainer) event.getGui()).getSlotUnderMouse();
            if (slot == null) return;
            boolean player = InventoryTweaksAPI.isPlayerSlot(((GuiContainer) event.getGui()).inventorySlots, slot.slotNumber);
            if (!player && !isSortableContainer(event.getGui())) return;
            NetworkHandler.sendToServer(new CSort(slot, player));
        }
    }

    public static boolean isSortableContainer(GuiScreen screen) {
        return screen instanceof GuiContainer && InventoryTweaksAPI.isValidSortable(((GuiContainer) screen).inventorySlots);
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
