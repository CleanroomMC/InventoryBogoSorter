package com.cleanroommc.bogosorter;

import static com.cleanroommc.bogosorter.ShortcutHandler.SetCanTakeStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.client.keybinds.KeyBind;
import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.config.ConfigGui;
import com.cleanroommc.bogosorter.common.config.SortRulesConfig;
import com.cleanroommc.bogosorter.common.config.TooltipFeatureConfig;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCube;
import com.cleanroommc.bogosorter.common.network.CAe2ContextRefresh;
import com.cleanroommc.bogosorter.common.network.CDropOff;
import com.cleanroommc.bogosorter.common.network.CSort;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.sort.ClientSortData;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.cleanroommc.bogosorter.compat.Mods;
import com.cleanroommc.bogosorter.compat.nei.Ae2TooltipClient;
import com.cleanroommc.bogosorter.compat.screen.WarningScreen;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;
import com.cleanroommc.modularui.api.event.KeyboardInputEvent;
import com.cleanroommc.modularui.api.event.MouseInputEvent;
import com.cleanroommc.modularui.factory.ClientGUI;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

public class ClientEventHandler {

    private static long timeConfigGui = 0;
    private static long timeSort = 0;
    private static long timeShortcut = 0;
    private static long timeDropoff = 0;
    private static long ticks = 0;
    private static long nextAe2ContextRefresh = 0;
    private static long ae2LoginWarmupUntil = 0;
    private static boolean hadClientWorld = false;
    private static final long AE2_CONTEXT_REFRESH_INTERVAL_MS = 30000L;
    private static final long AE2_CONTEXT_WARMUP_REFRESH_MS = 1000L;
    private static final long AE2_CONTEXT_MIN_THROTTLE_MS = 250L;
    private static final long AE2_LOGIN_WARMUP_MS = 30000L;
    private static final long AE2_PENDING_SEARCH_TTL_MS = 5000L;
    private static final int MIN_SEARCH_RESULT_COUNT = 1;
    private static final Class<?>[] NO_PARAMETERS = new Class<?>[0];
    private static final Class<?>[] STRING_PARAMETER = new Class<?>[] { String.class };
    private static final String AE2_MONITORABLE_GUI_CLASS = "appeng.client.gui.implementations.GuiMEMonitorable";
    private static final String NEI_CONTAINER_MANAGER_CLASS = "codechicken.nei.guihook.GuiContainerManager";
    private static final String NEI_SEARCH_FIELD_CLASS = "codechicken.nei.SearchField";

    private static GuiScreen nextGui = null;
    private static GuiContainer pendingAe2SearchGui = null;
    private static String pendingAe2SearchText = null;
    private static long pendingAe2SearchUntil = 0L;

    public static void openNextTick(GuiScreen screen) {
        ClientEventHandler.nextGui = screen;
    }

    public static long getTicks() {
        return ticks;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ticks++;
        }
        if (ClientEventHandler.nextGui != null) {
            ClientGUI.open(ClientEventHandler.nextGui);
            ClientEventHandler.nextGui = null;
        }
        updateAe2LoginWarmup();
        refreshAe2ContextIfNeeded();
        applyPendingAe2Search();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu && !WarningScreen.wasOpened) {
            WarningScreen.wasOpened = true;
            List<String> warnings = new ArrayList<>();
            if (Loader.isModLoaded("inventorytweaks")) {
                warnings.add("InventoryTweaks is loaded. This will cause issues!");
                warnings.add("Consider removing the mod and reload the game.");
            }
            if (!warnings.isEmpty()) {
                warnings.add(0, EnumChatFormatting.BOLD + "! Warning from Inventory Bogosorter !");
                warnings.add(1, "");
                event.gui = new WarningScreen(warnings);
            }
        }
    }

    private static void shortcutAction() {
        timeShortcut = Minecraft.getSystemTime();
    }

    private static boolean canDoShortcutAction() {
        return Minecraft.getSystemTime() - timeShortcut > 50;
    }

    // i have to subscribe to 4 events to catch all inputs

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        handleInput(null);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.MouseInputEvent event) {
        handleInput(null);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onGuiKeyInput(KeyboardInputEvent.Pre event) {
        KeyBind.checkKeys(getTicks());
        if (!(event.gui instanceof GuiContainer)) return;
        if (handleAe2TerminalSearchKey((GuiContainer) event.gui)) {
            event.setCanceled(true);
            return;
        }
        if (handleInput((GuiContainer) event.gui)) {
            event.setCanceled(true);
            return;
        }

        // Debug clear/randomize tools. The trigger follows the server-synced toggle; the server still
        // re-checks the toggle and operator status authoritatively before acting.
        if (BogoSorterConfig.enableDebugTools) {
            // clear
            if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1)) {
                SlotAccessor slot = getSlot(event.gui);
                SortHandler sortHandler = createSortHandler(event.gui, slot);
                if (sortHandler == null) return;
                sortHandler.clearAllItems(slot);
                return;
            }
            // random
            if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD2)) {
                SlotAccessor slot = getSlot(event.gui);
                SortHandler sortHandler = createSortHandler(event.gui, slot);
                if (sortHandler == null) return;
                sortHandler.randomizeItems(slot);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onMouseInput(MouseInputEvent.Pre event) {
        KeyBind.checkKeys(getTicks());
        if (event.gui instanceof GuiContainer && handleInput((GuiContainer) event.gui)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        if (BogoSorterConfig.dropOff.dropoffRender) {
            RendererCube.INSTANCE.tryToRender(event);
        }
    }

    // handle all inputs in one method
    public static boolean handleInput(@Nullable GuiContainer container) {

        if (container != null && canDoShortcutAction()) {
            KeyBind key;

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.MOVE_ALL);
            if (key != null && key.isFirstPress() && ShortcutHandler.moveAllItems(container, false)) {
                shortcutAction();
                return true;
            }

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.MOVE_ALL_SAME);
            if (key != null && key.isFirstPress() && ShortcutHandler.moveAllItems(container, true)) {
                shortcutAction();
                return true;
            }

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.MOVE_SINGLE);
            if (key != null && key.isFirstPressOrHeldLong(15) && ShortcutHandler.moveSingleItem(container, false)) {
                shortcutAction();
                return true;
            }

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.MOVE_SINGLE_EMPTY);
            if (key != null && key.isFirstPressOrHeldLong(15) && ShortcutHandler.moveSingleItem(container, true)) {
                shortcutAction();
                return true;
            }

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.THROW_ALL);
            if (key != null && key.isFirstPress() && ShortcutHandler.dropItems(container, false)) {
                shortcutAction();
                return true;
            }

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.THROW_ALL_SAME);
            if (key != null && key.isFirstPress() && ShortcutHandler.dropItems(container, true)) {
                shortcutAction();
                return true;
            }
            SetCanTakeStack = true;
        }
        if (Keypress(BSKeybinds.sortKeyOutsideGUI)) {
            long t = Minecraft.getSystemTime();
            if (t - timeSort > 500) {
                sort(Minecraft.getMinecraft().thePlayer.inventoryContainer, null, 9); // main inventory
                sort(Minecraft.getMinecraft().thePlayer.inventoryContainer, null, 36); // hotbar

                timeSort = t;
                return true;
            }
        }
        if (Keypress(BSKeybinds.sortKeyInGUI)) {
            long t = Minecraft.getSystemTime();
            if (t - timeSort > 500) {
                if (container != null) {
                    SlotAccessor slot = getSlot(container);
                    if (!canSort(slot) || !sort(container, slot)) {
                        return false;
                    }
                    timeSort = t;
                    return true;
                }
            }
        }
        if (Keypress(BSKeybinds.configGuiKey)) {
            long t = Minecraft.getSystemTime();
            if (t - timeConfigGui > 500) {
                if (!ConfigGui.closeCurrent()) {
                    BogoSortAPI.INSTANCE.openConfigGui(Minecraft.getMinecraft().currentScreen);
                }
                timeConfigGui = t;
            }
        }
        if (Keypress(BSKeybinds.dropoffKey)) {
            long t = Minecraft.getSystemTime();
            if (t - timeDropoff > BogoSorterConfig.dropOff.dropoffPacketThrottleInMS) {
                if (BogoSorterConfig.dropOff.enableDropOff) {
                    NetworkHandler.sendToServer(new CDropOff());
                }
                timeDropoff = t;
            }
        }
        return false;
    }

    private static boolean handleAe2TerminalSearchKey(GuiContainer gui) {
        if (!TooltipFeatureConfig.isTooltipEnabled() || !Mods.Ae2.isLoaded()) {
            return false;
        }
        if (!Keyboard.getEventKeyState() || Keyboard.getEventKey() != Keyboard.KEY_T) {
            return false;
        }
        GuiContainer ae2Gui = getAe2SearchTargetGui(gui);
        if (ae2Gui == null) {
            return false;
        }

        try {
            ItemStack hoveredStack = getHoveredStackForAe2Search(gui);
            if (hoveredStack == null) {
                return false;
            }

            Object searchField = getFieldValue(ae2Gui, "searchField");
            Object repo = getFieldValue(ae2Gui, "repo");
            if (searchField == null || repo == null) {
                return false;
            }

            Object focused = invokeMethod(searchField, "isFocused");
            if (focused instanceof Boolean && (Boolean) focused) {
                return false;
            }

            String displayName = hoveredStack.getDisplayName();
            if (displayName == null || displayName.trim()
                .isEmpty()) {
                return false;
            }

            String searchText = getEscapedNeiSearchText(displayName);
            String previousText = (String) invokeMethod(searchField, "getText");

            invokeMethod(repo, "setSearchString", STRING_PARAMETER, searchText);
            invokeMethod(repo, "updateView");

            Object size = invokeMethod(repo, "size");
            if (!(size instanceof Integer) || ((Integer) size).intValue() < MIN_SEARCH_RESULT_COUNT) {
                invokeMethod(repo, "setSearchString", STRING_PARAMETER, previousText);
                invokeMethod(repo, "updateView");
                return false;
            }

            if (gui != ae2Gui) {
                queuePendingAe2Search(ae2Gui, searchText);
                Minecraft.getMinecraft()
                    .displayGuiScreen(ae2Gui);
                return true;
            }

            invokeMethod(searchField, "setText", STRING_PARAMETER, searchText);
            invokeMethod(searchField, "setCursorPositionEnd");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void refreshAe2ContextIfNeeded() {
        if (!TooltipFeatureConfig.isTooltipEnabled() || !Mods.Ae2.isLoaded()) {
            Ae2TooltipClient.setAe2ContextAvailable(false);
            clearPendingAe2Search();
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        boolean warmingUp = Minecraft.getSystemTime() < ae2LoginWarmupUntil;
        if (!warmingUp && !(mc.currentScreen instanceof GuiContainer)) {
            return;
        }

        requestAe2ContextRefresh(warmingUp ? AE2_CONTEXT_WARMUP_REFRESH_MS : AE2_CONTEXT_REFRESH_INTERVAL_MS);
    }

    private static void updateAe2LoginWarmup() {
        if (!TooltipFeatureConfig.isTooltipEnabled() || !Mods.Ae2.isLoaded()) {
            Ae2TooltipClient.setAe2ContextAvailable(false);
            clearPendingAe2Search();
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        boolean hasWorld = mc.thePlayer != null && mc.theWorld != null;
        if (hasWorld && !hadClientWorld) {
            long now = Minecraft.getSystemTime();
            ae2LoginWarmupUntil = now + AE2_LOGIN_WARMUP_MS;
            nextAe2ContextRefresh = 0L;
            Ae2TooltipClient.setAe2ContextAvailable(false);
            clearPendingAe2Search();
        }
        hadClientWorld = hasWorld;
    }

    private static void requestAe2ContextRefresh(long throttleMillis) {
        if (!TooltipFeatureConfig.isTooltipEnabled()) {
            Ae2TooltipClient.setAe2ContextAvailable(false);
            clearPendingAe2Search();
            return;
        }
        long now = Minecraft.getSystemTime();
        if (throttleMillis > 0L && now < nextAe2ContextRefresh) {
            return;
        }

        nextAe2ContextRefresh = now + Math.max(throttleMillis, AE2_CONTEXT_MIN_THROTTLE_MS);
        NetworkHandler.sendToServer(new CAe2ContextRefresh());
    }

    @Nullable
    private static GuiContainer getAe2SearchTargetGui(GuiContainer gui) {
        if (isAe2MonitorableGui(gui)) {
            return gui;
        }

        try {
            Object firstGui = getFieldValue(gui, "firstGui");
            if (firstGui instanceof GuiContainer firstContainer && isAe2MonitorableGui(firstContainer)) {
                return firstContainer;
            }
        } catch (ReflectiveOperationException ignored) {}

        try {
            Object firstGui = invokeMethod(gui, "getFirstScreen");
            if (firstGui instanceof GuiContainer firstContainer && isAe2MonitorableGui(firstContainer)) {
                return firstContainer;
            }
        } catch (ReflectiveOperationException ignored) {}

        return null;
    }

    private static boolean isAe2MonitorableGui(GuiContainer gui) {
        Class<?> current = gui.getClass();
        while (current != null) {
            if (AE2_MONITORABLE_GUI_CLASS.equals(current.getName())) {
                return true;
            }
            current = current.getSuperclass();
        }
        return false;
    }

    @Nullable
    private static ItemStack getHoveredStackForAe2Search(GuiContainer gui) {
        if (Loader.isModLoaded("NotEnoughItems")) {
            try {
                ItemStack hoveredStack = getNeiHoveredStack(gui);
                if (hoveredStack != null) {
                    return hoveredStack;
                }
            } catch (Throwable ignored) {}
        }

        return gui.theSlot == null ? null : gui.theSlot.getStack();
    }

    @Nullable
    private static ItemStack getNeiHoveredStack(GuiContainer gui) throws Exception {
        Class<?> manager = Class.forName(NEI_CONTAINER_MANAGER_CLASS);
        Method method = manager.getMethod("getStackMouseOver", GuiContainer.class);
        Object result = method.invoke(null, gui);
        return result instanceof ItemStack ? (ItemStack) result : null;
    }

    private static String getEscapedNeiSearchText(String text) {
        try {
            Class<?> searchField = Class.forName(NEI_SEARCH_FIELD_CLASS);
            Method method = searchField.getMethod("getEscapedSearchText", String.class);
            Object result = method.invoke(null, text);
            if (result instanceof String) {
                return (String) result;
            }
        } catch (Throwable ignored) {}
        return text;
    }

    private static void applyPendingAe2Search() {
        if (!TooltipFeatureConfig.isTooltipEnabled()) {
            clearPendingAe2Search();
            return;
        }
        if (pendingAe2SearchGui == null || pendingAe2SearchText == null) {
            return;
        }
        if (Minecraft.getSystemTime() > pendingAe2SearchUntil) {
            clearPendingAe2Search();
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != pendingAe2SearchGui) {
            return;
        }

        try {
            Object searchField = getFieldValue(pendingAe2SearchGui, "searchField");
            Object repo = getFieldValue(pendingAe2SearchGui, "repo");
            if (searchField == null || repo == null) {
                return;
            }

            invokeMethod(repo, "setSearchString", STRING_PARAMETER, pendingAe2SearchText);
            invokeMethod(repo, "updateView");
            invokeMethod(searchField, "setText", STRING_PARAMETER, pendingAe2SearchText);
            invokeMethod(searchField, "setCursorPositionEnd");
        } catch (Throwable ignored) {} finally {
            clearPendingAe2Search();
        }
    }

    private static void queuePendingAe2Search(GuiContainer gui, String searchText) {
        pendingAe2SearchGui = gui;
        pendingAe2SearchText = searchText;
        pendingAe2SearchUntil = Minecraft.getSystemTime() + AE2_PENDING_SEARCH_TTL_MS;
    }

    private static void clearPendingAe2Search() {
        pendingAe2SearchGui = null;
        pendingAe2SearchText = null;
        pendingAe2SearchUntil = 0L;
    }

    @Nullable
    private static Object getFieldValue(Object instance, String fieldName) throws ReflectiveOperationException {
        Class<?> current = instance.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(instance);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    @Nullable
    private static Object invokeMethod(Object instance, String methodName) throws ReflectiveOperationException {
        return invokeMethod(instance, methodName, NO_PARAMETERS);
    }

    @Nullable
    private static Object invokeMethod(Object instance, String methodName, Class<?>[] paramTypes, Object... args)
        throws ReflectiveOperationException {
        Class<?> current = instance.getClass();
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName, paramTypes);
                method.setAccessible(true);
                return method.invoke(instance, args);
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }

        Method method = instance.getClass()
            .getMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(instance, args);
    }

    private static boolean canSort(@Nullable SlotAccessor slot) {
        return !Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode
            || (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null
                && (slot == null || slot.callGetStack() == null));
    }

    private static boolean isButtonPressed(int button) {
        return Mouse.getEventButtonState() && Mouse.getEventButton() == button;
    }

    private static boolean isKeyDown(KeyBinding key) {
        if (key.getKeyCode() == 0) return false;

        if (key.getKeyCode() < 0) {
            return isButtonPressed(key.getKeyCode() + 100);
        }
        return Keyboard.getEventKeyState() && Keyboard.getEventKey() == key.getKeyCode();
    }

    public static boolean isSortableContainer(GuiScreen screen) {
        return screen instanceof GuiContainer && BogoSortAPI.isValidSortable(((GuiContainer) screen).inventorySlots);
    }

    public static <T extends Container & ISortableContainer> T getSortableContainer(GuiScreen screen) {
        return (T) ((GuiContainer) screen).inventorySlots;
    }

    @Nullable
    public static SlotAccessor getSlot(GuiScreen guiScreen) {
        if (guiScreen instanceof GuiContainer) {
            return (SlotAccessor) ((GuiContainer) guiScreen).theSlot;
        }
        return null;
    }

    public static boolean sort(GuiScreen guiScreen, @Nullable SlotAccessor slot) {
        if (guiScreen instanceof GuiContainer) {
            return sort(((GuiContainer) guiScreen).inventorySlots, slot, -1);
        }
        return false;
    }

    public static boolean sort(Container container, @Nullable SlotAccessor slot, int slotNumber) {
        GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);
        if (sortingContext.isEmpty()) return false;
        SlotGroup slotGroup = null;
        if (slot == null && slotNumber == -1) {
            if (sortingContext.getNonPlayerSlotGroupAmount() == 1) {
                slotGroup = sortingContext.getNonPlayerSlotGroup();
            } else if (sortingContext.hasPlayer() && sortingContext.getNonPlayerSlotGroupAmount() == 0) {
                slotGroup = sortingContext.getPlayerSlotGroup();
            }
            if (slotGroup == null || slotGroup.isEmpty()) return false;
            slot = slotGroup.getSlots()
                .get(0);
        } else {
            slotGroup = sortingContext.getSlotGroup(slot != null ? slot.getSlotNumber() : slotNumber);
            if (slotGroup == null || slotGroup.isEmpty()
                || (slotGroup.isHotbar() && !BogoSorterConfig.enableHotbarSort)) return false;
        }

        List<SortRule<ItemStack>> sortRules = SortRulesConfig.sortRules;
        boolean color = sortRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("color"));
        boolean name = sortRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("display_name"));
        NetworkHandler.sendToServer(
            new CSort(
                createSortData(slotGroup, color, name),
                SortRulesConfig.sortRules,
                SortRulesConfig.nbtSortRules,
                slot != null ? slot.getSlotNumber() : slotNumber,
                slotGroup.isPlayerInventory()));
        SortHandler.playSortSound();

        return true;

    }

    public static Collection<ClientSortData> createSortData(SlotGroup slotGroup, boolean color, boolean name) {
        if (!color && !name) return Collections.emptyList();
        Map<ItemStack, ClientSortData> map = new Object2ObjectOpenCustomHashMap<>(
            BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);
        for (SlotAccessor slot1 : slotGroup.getSlots()) {
            map.computeIfAbsent(slot1.callGetStack(), stack -> ClientSortData.of(stack, color, name))
                .getSlotNumbers()
                .add(slot1.getSlotNumber());
        }
        return map.values();
    }

    public static SortHandler createSortHandler(GuiScreen guiScreen, @Nullable SlotAccessor slot) {
        if (slot != null && guiScreen instanceof GuiContainer) {

            Container container = ((GuiContainer) guiScreen).inventorySlots;
            boolean player = BogoSortAPI.isPlayerSlot(slot);

            if (!player && !isSortableContainer(guiScreen)) return null;

            return new SortHandler(
                Minecraft.getMinecraft().thePlayer,
                container,
                SortRulesConfig.sortRules,
                SortRulesConfig.nbtSortRules,
                Int2ObjectMaps.emptyMap());
        }
        return null;
    }

    private static boolean Keypress(KeyBinding key) {
        int keyCode = key.getKeyCode();
        if (keyCode == 0) return false;
        if (keyCode > 0) {
            return key.isPressed() || Keyboard.isKeyDown(keyCode);
        } else {
            return Mouse.isButtonDown(100 + keyCode);
        }
    }
}
