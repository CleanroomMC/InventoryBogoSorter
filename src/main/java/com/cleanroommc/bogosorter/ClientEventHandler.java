package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = BogoSorter.ID, value = Side.CLIENT)
public class ClientEventHandler {

    public static final List<ItemStack> allItems = new ArrayList<>();

    @SubscribeEvent
    public static void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Post event) {
        if (!(event.getGui() instanceof GuiContainer)) return;
        if (FMLLaunchHandler.isDeobfuscatedEnvironment()) {
            // clear
            if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD0)) {
                Slot slot = getSlot(event.getGui());
                SortHandler sortHandler = createSortHandler(event.getGui(), slot);
                if (sortHandler == null) return;
                sortHandler.clearAllItems(slot);
                return;
            }
            // random
            if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1)) {
                if (allItems.isEmpty()) {
                    for (Item item : ForgeRegistries.ITEMS) {
                        NonNullList<ItemStack> subItems = NonNullList.create();
                        item.getSubItems(CreativeTabs.SEARCH, subItems);
                        allItems.addAll(subItems);
                    }
                }
                Slot slot = getSlot(event.getGui());
                SortHandler sortHandler = createSortHandler(event.getGui(), slot);
                if (sortHandler == null) return;
                sortHandler.randomizeItems(slot);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseInput(GuiScreenEvent.MouseInputEvent.Post event) {
        if (Mouse.isButtonDown(2) && event.getGui() instanceof GuiContainer) {
            Slot slot = getSlot(event.getGui());
            if (slot == null || (Minecraft.getMinecraft().player.isCreative() && !slot.getStack().isEmpty())) return;
            SortHandler sortHandler = createSortHandler(event.getGui(), slot);
            if (sortHandler == null) return;
            sortHandler.sort(slot.slotNumber);
        }
    }

    public static boolean isSortableContainer(GuiScreen screen) {
        return screen instanceof GuiContainer && BogoSortAPI.isValidSortable(((GuiContainer) screen).inventorySlots);
    }

    public static <T extends Container & ISortableContainer> T getSortableContainer(GuiScreen screen) {
        return (T) ((GuiContainer) screen).inventorySlots;
    }

    @Nullable
    public static Slot getSlot(GuiScreen guiScreen) {
        if (guiScreen instanceof GuiContainer) {
            return ((GuiContainer) guiScreen).getSlotUnderMouse();
        }
        return null;
    }

    public static SortHandler createSortHandler(GuiScreen guiScreen, @Nullable Slot slot) {
        if (slot != null && guiScreen instanceof GuiContainer) {

            Container container = ((GuiContainer) guiScreen).inventorySlots;
            boolean player = BogoSortAPI.INSTANCE.isPlayerSlot(container, slot);

            if (!player && !isSortableContainer(guiScreen)) return null;

            return new SortHandler(container, player);
        }
        return null;
    }
}
