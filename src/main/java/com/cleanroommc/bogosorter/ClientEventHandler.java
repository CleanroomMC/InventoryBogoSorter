package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.common.config.ConfigGui;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.cleanroommc.modularui.api.UIInfos;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = BogoSorter.ID, value = Side.CLIENT)
public class ClientEventHandler {

    public static final List<ItemStack> allItems = new ArrayList<>();
    public static final KeyBinding configGuiKey = new KeyBinding("key.sort_config", KeyConflictContext.UNIVERSAL, Keyboard.KEY_K, "key.categories.bogosorter");
    public static final KeyBinding sortKey = new KeyBinding("key.sort", KeyConflictContext.GUI, -98, "key.categories.bogosorter");

    private static long timeConfigGui = 0;
    private static long timeSort = 0;

    // i have to subscribe to 4 events to catch all inputs

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        handleInput(null);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.MouseInputEvent event) {
        handleInput(null);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGuiKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!(event.getGui() instanceof GuiContainer)) return;
        if (handleInput((GuiContainer) event.getGui())) {
            event.setCanceled(true);
            return;
        }

        if (FMLLaunchHandler.isDeobfuscatedEnvironment()) {
            // clear
            if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1)) {
                Slot slot = getSlot(event.getGui());
                SortHandler sortHandler = createSortHandler(event.getGui(), slot);
                if (sortHandler == null) return;
                sortHandler.clearAllItems(slot);
                return;
            }
            // random
            if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD2)) {
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

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (event.getGui() instanceof GuiContainer && handleInput((GuiContainer) event.getGui())) {
            event.setCanceled(true);
        }
    }

    // handle all inputs in one method
    public static boolean handleInput(@Nullable GuiContainer container) {
        if (container != null && container.isFocused()) {
            return false;
        }
        boolean c = configGuiKey.isPressed(), s = sortKey.isPressed();
        if (c) {
            long t = Minecraft.getSystemTime();
            if (t - timeConfigGui > 500) {
                if (Minecraft.getMinecraft().currentScreen != null && Minecraft.getMinecraft().currentScreen instanceof ModularGui){
                    Minecraft.getMinecraft().displayGuiScreen(null);
                } else {
                    UIInfos.openClientUI(Minecraft.getMinecraft().player, ConfigGui::createConfigWindow);
                }
                timeConfigGui = t;
                return true;
            }
        }
        if (container != null && s) {
            long t = Minecraft.getSystemTime();
            if (t - timeSort > 500) {
                Slot slot = getSlot(container);
                if (slot == null || !canSort(slot))
                    return false;
                SortHandler sortHandler = createSortHandler(container, slot);
                if (sortHandler == null) return false;
                sortHandler.sort(slot.slotNumber);
                timeSort = t;
                return true;
            }
        }
        return false;
    }

    private static boolean canSort(Slot slot) {
        return !Minecraft.getMinecraft().player.isCreative() ||
                sortKey.getKeyModifier().isActive() != Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getKeyModifier().isActive() ||
                sortKey.getKeyCode() != Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getKeyCode() ||
                (slot.getStack().isEmpty() && Minecraft.getMinecraft().player.inventory.getItemStack().isEmpty());
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
