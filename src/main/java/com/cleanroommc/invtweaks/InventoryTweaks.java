package com.cleanroommc.invtweaks;

import com.cleanroommc.invtweaks.api.ISortableContainer;
import com.cleanroommc.invtweaks.api.InventoryTweaksAPI;
import com.cleanroommc.invtweaks.compat.DefaultCompat;
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
        DefaultCompat.init();
    }

    @SubscribeEvent
    public static void onGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiContainer) {
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
