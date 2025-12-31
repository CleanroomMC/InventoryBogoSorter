package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.config.ConfigGui;
import com.cleanroommc.bogosorter.common.lock.SlotLock;
import com.cleanroommc.bogosorter.common.network.CSort;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.sort.ClientSortData;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.cleanroommc.bogosorter.compat.screen.WarningScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.kbp.client.KBPMod;
import com.kbp.client.api.IPatchedKeyBinding;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    public static final List<ItemStack> allItems = new ArrayList<>();
    public static final String BOGO_CATEGORY = "bogosort.key.categories";
    public static final String KEY_PREFIX = "bogosort.key.";
    public static final KeyBinding configGuiKey = new KeyBinding(KEY_PREFIX + "sort_config", KeyConflictContext.UNIVERSAL, Keyboard.KEY_K, BOGO_CATEGORY);
    public static final KeyBinding sortKey = new KeyBinding(KEY_PREFIX + "sort", KeyConflictContext.GUI, -98, BOGO_CATEGORY);
    public static final KeyBinding keyLockSlot = new KeyBinding(KEY_PREFIX + "lock_slot", KeyConflictContext.GUI, 19, BOGO_CATEGORY);
    public static final KeyBinding keyDropReplacement = new KeyBinding("key.drop", 16, "key.categories.inventory") {
        public boolean isActiveAndMatches(int keyCode) {
            if (!super.isActiveAndMatches(keyCode)) return false;
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player != null && Minecraft.getMinecraft().currentScreen == null) {
                return !SlotLock.getClientCap().isSlotLocked(player.inventory.currentItem);
            }
            return true;
        }

        public boolean isKeyDown() {
            if (!super.isKeyDown()) return false;
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player != null && Minecraft.getMinecraft().currentScreen == null) {
                return !SlotLock.getClientCap().isSlotLocked(player.inventory.currentItem);
            }
            return true;
        }
    };

    public static final KeyBinding keySwapHandReplacement = new KeyBinding("key.swapHands", 33, "key.categories.inventory") {
        public boolean isActiveAndMatches(int keyCode) {
            if (!super.isActiveAndMatches(keyCode)) return false;
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player != null && Minecraft.getMinecraft().currentScreen == null) {
                return !SlotLock.getClientCap().isSlotLocked(player.inventory.currentItem) && !SlotLock.getClientCap().isSlotLocked(40);
            }
            return true;
        }

        public boolean isKeyDown() {
            if (!super.isKeyDown()) return false;
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player != null && Minecraft.getMinecraft().currentScreen == null) {
                return !SlotLock.getClientCap().isSlotLocked(player.inventory.currentItem) && !SlotLock.getClientCap().isSlotLocked(40);
            }
            return true;
        }
    };

    public static final int LMB = 0;
    public static final int RMB = 1;
    public static final int WHEEL = 2;
    public static final int ALT = Keyboard.KEY_LMENU;
    public static final int CTRL = Minecraft.IS_RUNNING_ON_MAC ? 219 : 29;
    public static final int SHIFT = Keyboard.KEY_LSHIFT;
    public static final int SPACE = Keyboard.KEY_SPACE;

    // we need to track press time ourselves, because mc is too stupid to do it properly
    private static final int[] timeTracker = new int[6];

    private static final int moveAllSameIndex = 0;
    private static final int moveAllIndex = 1;
    private static final int moveSingleIndex = 2;
    private static final int moveSingleEmptyIndex = 3;
    private static final int throwAllSameIndex = 4;
    private static final int throwAllIndex = 5;

    public static final IPatchedKeyBinding moveAllSame = KBPMod.newBuilder(KEY_PREFIX + "move_all_same")
            .withCategory(BOGO_CATEGORY)
            .withMouseButton(LMB)
            .withCmbKeys(ALT)
            .withConflictContext(KeyConflictContext.GUI)
            .buildAndRegis();
    public static final IPatchedKeyBinding moveAll = KBPMod.newBuilder(KEY_PREFIX + "move_all")
            .withCategory(BOGO_CATEGORY)
            .withMouseButton(LMB)
            .withCmbKeys(SPACE)
            .withConflictContext(KeyConflictContext.GUI)
            .buildAndRegis();
    public static final IPatchedKeyBinding moveSingle = KBPMod.newBuilder(KEY_PREFIX + "move_single")
            .withCategory(BOGO_CATEGORY)
            .withMouseButton(LMB)
            .withCmbKeys(CTRL)
            .withConflictContext(KeyConflictContext.GUI)
            .buildAndRegis();
    public static final IPatchedKeyBinding moveSingleEmpty = KBPMod.newBuilder(KEY_PREFIX + "move_single_empty")
            .withCategory(BOGO_CATEGORY)
            .withMouseButton(RMB)
            .withCmbKeys(CTRL)
            .withConflictContext(KeyConflictContext.GUI)
            .buildAndRegis();
    public static final IPatchedKeyBinding throwAllSame = KBPMod.newBuilder(KEY_PREFIX + "throw_all_same")
            .withCategory(BOGO_CATEGORY)
            .withKey(Keyboard.KEY_Q)
            .withCmbKeys(ALT)
            .withConflictContext(KeyConflictContext.GUI)
            .buildAndRegis();
    public static final IPatchedKeyBinding throwAll = KBPMod.newBuilder(KEY_PREFIX + "throw_all")
            .withCategory(BOGO_CATEGORY)
            .withKey(Keyboard.KEY_Q)
            .withCmbKeys(SPACE)
            .withConflictContext(KeyConflictContext.GUI)
            .buildAndRegis();

    static {
        Arrays.fill(timeTracker, -1);
    }

    private static long timeConfigGui = 0;
    private static long timeSort = 0;
    private static long timeShortcut = 0;
    private static long ticks = 0;
    private static long lastTickChecked = -1;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu && !WarningScreen.wasOpened) {
            WarningScreen.wasOpened = true;
            List<String> warnings = new ArrayList<>();
            if (Loader.isModLoaded("inventorytweaks")) {
                warnings.add("- InventoryTweaks is loaded. This will cause issues!");
                warnings.add("Consider removing the mod and reload the game.");
            }
            if (BogoSorter.Mods.ITEM_FAVORITES.isLoaded()) {
                warnings.add("- Item Favorites is loaded. BogoSorter implements all its features and is obsolete.");
                warnings.add("Consider removing the mod and reload the game.");
            }
            if (!warnings.isEmpty()) {
                warnings.add(0, TextFormatting.BOLD + "! Warning from Inventory Bogosorter !");
                warnings.add(1, "");
                event.setGui(new WarningScreen(warnings));
            }
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ticks++;
            if (Minecraft.getMinecraft().world != null) checkInput();
        }
    }

    private static void checkInput() {
        if (lastTickChecked != ticks) {
            checkKey(moveAllSameIndex, moveAllSame);
            checkKey(moveAllIndex, moveAll);
            checkKey(moveSingleIndex, moveSingle);
            checkKey(moveSingleEmptyIndex, moveSingleEmpty);
            checkKey(throwAllSameIndex, throwAllSame);
            checkKey(throwAllIndex, throwAll);
        }
    }

    private static boolean isPressed(int i) {
        return timeTracker[i] == 0;
    }

    private static void checkKey(int i, IPatchedKeyBinding key) {
        if (key.getKeyBinding().isKeyDown()) {
            timeTracker[i]++;
        } else {
            timeTracker[i] = -1;
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
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        checkInput();
        handleInput(null);
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseInputEvent event) {
        checkInput();
        handleInput(null);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGuiKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        checkInput();
        if (!(event.getGui() instanceof GuiContainer gui)) return;
        if (handleInput(gui) || SlotLock.onGuiKeyInput(gui)) {
            event.setCanceled(true);
            return;
        }

        if (FMLLaunchHandler.isDeobfuscatedEnvironment()) {
            // clear
            if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1)) {
                ISlot slot = getSlot(gui);
                SortHandler sortHandler = createSortHandler(gui, slot);
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
                ISlot slot = getSlot(gui);
                SortHandler sortHandler = createSortHandler(gui, slot);
                if (sortHandler == null) return;
                sortHandler.randomizeItems(slot);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGuiMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        checkInput();
        if (event.getGui() instanceof GuiContainer gui && (handleInput(gui) || SlotLock.onGuiMouseInput(gui))) {
            event.setCanceled(true);
        }
    }

    // handle all inputs in one method
    public static boolean handleInput(@Nullable GuiContainer container) {
        if (container != null && container.isFocused()) {
            return false;
        }
        if (container != null && canDoShortcutAction()) {
            if (isPressed(moveAllIndex) && ShortcutHandler.moveAllItems(container, false)) {
                shortcutAction();
                return true;
            }
            if (isPressed(moveAllSameIndex) && ShortcutHandler.moveAllItems(container, true)) {
                shortcutAction();
                return true;
            }
            // TODO should also activate after holding for 15 ticks
            if ((isPressed(moveSingleIndex)) && ShortcutHandler.moveSingleItem(container, false)) {
                shortcutAction();
                return true;
            }
            // TODO should also activate after holding for 15 ticks
            if ((isPressed(moveSingleEmptyIndex)) && ShortcutHandler.moveSingleItem(container, true)) {
                shortcutAction();
                return true;
            }
            if (isPressed(throwAllIndex) && ShortcutHandler.dropItems(container, false)) {
                shortcutAction();
                return true;
            }
            if (isPressed(throwAllSameIndex) && ShortcutHandler.dropItems(container, true)) {
                shortcutAction();
                return true;
            }
        }
        boolean c = configGuiKey.isPressed(), s = sortKey.isPressed();
        if (c) {
            long t = Minecraft.getSystemTime();
            if (t - timeConfigGui > 500) {
                if (!ConfigGui.closeCurrent()) {
                    BogoSortAPI.INSTANCE.openConfigGui();
                }
                timeConfigGui = t;
                return true;
            }
        }
        if (container != null && s) {
            long t = Minecraft.getSystemTime();
            if (t - timeSort > 500) {
                ISlot slot = getSlot(container);
                if (!canSort(slot) || !sort(container, slot)) {
                    return false;
                }
                timeSort = t;
                return true;
            }
        }
        return false;
    }

    private static boolean canSort(@Nullable ISlot slot) {
        return !Minecraft.getMinecraft().player.isCreative() ||
                sortKey.getKeyModifier().isActive(null) !=
                        Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getKeyModifier().isActive(null) ||
                sortKey.getKeyCode() != Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getKeyCode() ||
                (Minecraft.getMinecraft().player.inventory.getItemStack().isEmpty() && (slot == null || slot.bogo$getStack().isEmpty()));
    }

    private static boolean isButtonPressed(int button) {
        return Mouse.getEventButtonState() && Mouse.getEventButton() == button;
    }

    private static boolean isKeyDown(KeyBinding key) {
        if (!key.getKeyModifier().isActive(null)) return false;
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
    public static ISlot getSlot(GuiContainer gui) {
        return (ISlot) gui.getSlotUnderMouse();
    }

    public static boolean sort(GuiScreen guiScreen, @Nullable ISlot slot) {
        if (guiScreen instanceof GuiContainer) {
            if (!SortHandler.enableHotbarSorting && BogoSortAPI.isPlayerHotbarSlot(slot)) {
                return false;
            }
            Container container = ((GuiContainer) guiScreen).inventorySlots;
            GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container, Minecraft.getMinecraft().player);
            if (sortingContext.isEmpty()) return false;
            SlotGroup slotGroup = null;
            if (slot == null) {
                if (sortingContext.getNonPlayerSlotGroupAmount() == 1) {
                    slotGroup = sortingContext.getNonPlayerSlotGroup();
                } else if (sortingContext.hasPlayer() && sortingContext.getNonPlayerSlotGroupAmount() == 0) {
                    slotGroup = sortingContext.getPlayerSlotGroup();
                }
                if (slotGroup == null || slotGroup.isEmpty()) return false;
                slot = slotGroup.getSlots().get(0);
            } else {
                slotGroup = sortingContext.getSlotGroup(slot.bogo$getSlotNumber());
                if (slotGroup == null || slotGroup.isEmpty()) return false;
            }

            List<SortRule<ItemStack>> sortRules = BogoSorterConfig.sortRules;
            boolean color = sortRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("color"));
            boolean name = sortRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("display_name"));
            NetworkHandler.sendToServer(
                    new CSort(createSortData(slotGroup, color, name), BogoSorterConfig.sortRules, BogoSorterConfig.nbtSortRules,
                            slot.bogo$getSlotNumber(), slotGroup.isPlayerInventory()));
            SortHandler.playSortSound();
            return true;
        }
        return false;
    }

    public static Collection<ClientSortData> createSortData(SlotGroup slotGroup, boolean color, boolean name) {
        if (!color && !name) return Collections.emptyList();
        Map<ItemStack, ClientSortData> map = new Object2ObjectOpenCustomHashMap<>(BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);
        for (ISlot slot1 : slotGroup.getSlots()) {
            map.computeIfAbsent(slot1.bogo$getStack(), stack -> ClientSortData.of(stack, color, name))
                    .getSlotNumbers()
                    .add(slot1.bogo$getSlotNumber());
        }
        return map.values();
    }

    public static SortHandler createSortHandler(GuiContainer guiScreen, @Nullable ISlot slot) {
        if (slot != null) {

            Container container = guiScreen.inventorySlots;
            boolean player = BogoSortAPI.isPlayerMainInvSlot(slot);

            if (!player && !isSortableContainer(guiScreen)) return null;

            return new SortHandler(Minecraft.getMinecraft().player, container, Int2ObjectMaps.emptyMap());
        }
        return null;
    }

    @SubscribeEvent
    public static void onRenderOverlayEvent(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE && e.getType() != RenderGameOverlayEvent.ElementType.JUMPBAR) {
            return;
        }
        if (Minecraft.getMinecraft().world != null && Minecraft.getMinecraft().player != null && !Minecraft.getMinecraft().player.isSpectator()) {
            SlotLock.drawHotbarOverlay();
        }
    }
}
