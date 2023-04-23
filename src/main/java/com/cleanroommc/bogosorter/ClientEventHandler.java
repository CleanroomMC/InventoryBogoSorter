package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.config.ConfigGui;
import com.cleanroommc.bogosorter.common.network.CSort;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.sort.ClientSortData;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.manager.GuiManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientEventHandler {

    public static final List<ItemStack> allItems = new ArrayList<>();
    public static final KeyBinding configGuiKey = new KeyBinding("key.sort_config", KeyConflictContext.UNIVERSAL, Keyboard.KEY_K, "key.categories.bogosorter");
    public static final KeyBinding sortKey = new KeyBinding("key.sort", KeyConflictContext.GUI, -98, "key.categories.bogosorter");

    private static long timeConfigGui = 0;
    private static long timeSort = 0;
    private static long timeShortcut = 0;

    private static void shortcutAction() {
        timeShortcut = Minecraft.getSystemTime();
    }

    private static boolean canDoShortcutAction() {
        return Minecraft.getSystemTime() - timeShortcut > 50;
    }

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

    private static GuiScreen previousScreen;

    // handle all inputs in one method
    public static boolean handleInput(@Nullable GuiContainer container) {
        if (container != null && container.isFocused()) {
            return false;
        }
        if (container != null && canDoShortcutAction()) {
            if (Mouse.isButtonDown(0) && !Mouse.isButtonDown(1)) {
                if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !GuiScreen.isAltKeyDown() && ShortcutHandler.moveAllItems(container, false)) {
                    shortcutAction();
                    return true;
                }
                if (!Keyboard.isKeyDown(Keyboard.KEY_SPACE) && GuiScreen.isAltKeyDown() && ShortcutHandler.moveAllItems(container, true)) {
                    shortcutAction();
                    return true;
                }
            }
            if (GuiScreen.isCtrlKeyDown()) {
                if (Mouse.isButtonDown(0) && !Mouse.isButtonDown(1) && ShortcutHandler.moveSingleItem(container, false)) {
                    shortcutAction();
                    return true;
                }
                if (Mouse.isButtonDown(1) && !Mouse.isButtonDown(0) && ShortcutHandler.moveSingleItem(container, true)) {
                    shortcutAction();
                    return true;
                }
            }
            if (isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindDrop)) {
                if (!Keyboard.isKeyDown(Keyboard.KEY_SPACE) && GuiScreen.isAltKeyDown() && ShortcutHandler.dropItems(container, true)) {
                    shortcutAction();
                    return true;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !GuiScreen.isAltKeyDown() && ShortcutHandler.dropItems(container, false)) {
                    shortcutAction();
                    return true;
                }
            }
        }
        boolean c = configGuiKey.isPressed(), s = sortKey.isPressed();
        if (c) {
            long t = Minecraft.getSystemTime();
            if (t - timeConfigGui > 500) {
                if (ConfigGui.wasOpened) {
                    Minecraft.getMinecraft().displayGuiScreen(previousScreen);
                    previousScreen = null;
                } else {
                    previousScreen = Minecraft.getMinecraft().currentScreen;
                    GuiManager.openClientUI(Minecraft.getMinecraft().player, new ConfigGui());
                }
                timeConfigGui = t;
                return true;
            }
        }
        if (container != null && s) {
            long t = Minecraft.getSystemTime();
            if (t - timeSort > 500) {
                Slot slot = getSlot(container);
                if (!canSort(slot) || !sort(container, slot)) {
                    return false;
                }
                timeSort = t;
                return true;
            }
        }
        return false;
    }

    private static boolean canSort(@Nullable Slot slot) {
        return !Minecraft.getMinecraft().player.isCreative() ||
                sortKey.getKeyModifier().isActive() != Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getKeyModifier().isActive() ||
                sortKey.getKeyCode() != Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getKeyCode() ||
                (Minecraft.getMinecraft().player.inventory.getItemStack().isEmpty() && (slot == null || slot.getStack().isEmpty()));
    }

    private static boolean isKeyDown(KeyBinding key) {
        return key.getKeyModifier().isActive() && (key.getKeyCode() < 0 ? Mouse.isButtonDown(key.getKeyCode() + 100) : Keyboard.isKeyDown(key.getKeyCode()));
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

    public static boolean sort(GuiScreen guiScreen, @Nullable Slot slot) {
        if (guiScreen instanceof GuiContainer) {
            Container container = ((GuiContainer) guiScreen).inventorySlots;
            GuiSortingContext sortingContext = GuiSortingContext.create(container);
            if (sortingContext.isEmpty()) return false;
            Slot[][] slotGroup = null;
            if (slot == null) {
                if (sortingContext.getNonPlayerSlotGroupAmount() == 1) {
                    slotGroup = sortingContext.getNonPlayerSlotGroup();
                } else if (sortingContext.hasPlayer() && sortingContext.getNonPlayerSlotGroupAmount() == 0) {
                    slotGroup = sortingContext.getPlayerSlotGroup();
                }
                if (slotGroup == null) return false;
                slot = slotGroup[0][0];
            } else {
                slotGroup = sortingContext.getSlotGroup(slot.slotNumber);
                if (slotGroup == null) return false;
            }
            boolean player = BogoSortAPI.isPlayerSlot(slot);

            List<SortRule<ItemStack>> sortRules = BogoSorterConfig.sortRules;
            boolean color = sortRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("color"));
            boolean name = sortRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("display_name"));
            NetworkHandler.sendToServer(new CSort(createSortData(slotGroup, color, name), BogoSorterConfig.sortRules, BogoSorterConfig.nbtSortRules, slot.slotNumber, player));
            Interactable.playButtonClickSound();
            return true;
        }
        return false;
    }

    public static List<ClientSortData> createSortData(Slot[][] slotGroup, boolean color, boolean name) {
        if (!color && !name) return Collections.emptyList();
        List<ClientSortData> data = new ArrayList<>();
        for (Slot[] slotRow : slotGroup) {
            for (Slot slot1 : slotRow) {
                data.add(ClientSortData.of(slot1, color, name));
            }
        }
        return data;
    }

    public static SortHandler createSortHandler(GuiScreen guiScreen, @Nullable Slot slot) {
        if (slot != null && guiScreen instanceof GuiContainer) {

            Container container = ((GuiContainer) guiScreen).inventorySlots;
            boolean player = BogoSortAPI.isPlayerSlot(slot);

            if (!player && !isSortableContainer(guiScreen)) return null;

            return new SortHandler(Minecraft.getMinecraft().player, container, player, Int2ObjectMaps.emptyMap());
        }
        return null;
    }
}
