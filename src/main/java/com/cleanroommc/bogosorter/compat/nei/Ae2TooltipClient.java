package com.cleanroommc.bogosorter.compat.nei;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.bogosorter.common.ReadableNumberConverter;
import com.cleanroommc.bogosorter.common.config.TooltipFeatureConfig;
import com.cleanroommc.bogosorter.common.network.CAe2AmountBatchRequest;
import com.cleanroommc.bogosorter.common.network.CAe2ContextRefresh;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.SAe2AmountResponse;
import com.cleanroommc.bogosorter.compat.Mods;
import com.cleanroommc.bogosorter.compat.ThaumicEnergisticsHelper;

import codechicken.nei.PositionedStack;
import codechicken.nei.Widget;
import codechicken.nei.WidgetContainer;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.NEIRecipeWidget;
import codechicken.nei.recipe.StackInfo;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class Ae2TooltipClient {

    private static final long CACHE_TTL_MS = 3000L;
    private static final long MISS_TTL_MS = 1500L;
    private static final long NO_SYSTEM_TTL_MS = 5000L;
    private static final long REQUEST_RETRY_MS = 2000L;
    private static final long FLUID_CONTAINER_TTL_MS = 60000L;
    private static final long BATCH_FLUSH_MS = 100L;
    private static final long REQUEST_TIMEOUT_MS = 15000L;
    private static final long CLIENT_CACHE_CLEANUP_MS = 30000L;
    private static final long CONTEXT_REFRESH_INTERVAL_MS = 30000L;
    private static final long TOOLTIP_CONTEXT_REFRESH_MS = 1500L;
    private static final int CLIENT_REQUESTS_PER_SECOND = 8;
    private static final int CLIENT_REQUEST_BURST = 16;
    private static final int MAX_BATCH_SIZE = 32;
    private static final int MAX_CLIENT_CACHE_ENTRIES = 4096;
    private static final int MAX_FLUID_CACHE_ENTRIES = 1024;
    private static final int KIND_ITEM = 0;
    private static final int KIND_FLUID = 1;
    private static final int KIND_ESSENTIA = 2;
    private static final int HOT_CACHE_HIT_THRESHOLD = 10;
    private static final long HOT_CACHE_TTL_MULTIPLIER = 2L;
    private static final double MILLIS_PER_SECOND = 1000.0D;
    private static final double REQUEST_TOKEN_COST = 1.0D;
    private static final int SINGLE_STACK_SIZE = 1;
    private static final int SINGLE_FLUID_AMOUNT = 1;

    private static final Map<String, Entry> CACHE = new HashMap<>();
    private static final Map<String, FluidEntry> FLUID_CONTAINER_CACHE = new HashMap<>();
    private static final Map<Integer, String> REQUEST_KEYS = new HashMap<>();
    private static final Map<String, PendingRequest> BATCH_QUEUE = new HashMap<>();
    private static int nextRequestId = 1;
    private static boolean initialized;
    private static double requestTokens = CLIENT_REQUEST_BURST;
    private static long lastRequestRefillTime;
    private static long nextBatchFlushTime;
    private static long nextClientCacheCleanupTime;
    private static long nextTooltipContextRefreshTime;
    private static int ae2ContextStatus = SAe2AmountResponse.STATUS_NO_SYSTEM;

    private Ae2TooltipClient() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        GuiContainerManager.addTooltipHandler(new RecipeTooltipHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(new Ae2GuiWatcher());
    }

    public static void appendAmountTooltip(ItemStack stack, List<String> tooltip) {
        appendAmountTooltip(stack, tooltip, false);
    }

    public static void appendAmountTooltip(ItemStack stack, List<String> tooltip, boolean allowOpenTerminal) {
        if (!TooltipFeatureConfig.isTooltipEnabled()) {
            return;
        }
        if (stack == null || stack.getItem() == null || !Mods.Ae2.isLoaded()) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        long now = Minecraft.getSystemTime();
        if (!allowOpenTerminal && ae2ContextStatus == SAe2AmountResponse.STATUS_OUT_OF_RANGE) {
            requestContextRefresh(now, TOOLTIP_CONTEXT_REFRESH_MS);
            addOutOfRangeLine(tooltip);
            return;
        }
        if (!allowOpenTerminal && ae2ContextStatus != SAe2AmountResponse.STATUS_OK) {
            requestContextRefresh(now, TOOLTIP_CONTEXT_REFRESH_MS);
            return;
        }

        FluidStack fluidStack = fluidOf(stack, now);
        String essentiaAspectTag = fluidStack == null ? ThaumicEnergisticsHelper.getAspectTag(stack) : null;
        int amountKind = fluidStack != null ? KIND_FLUID : essentiaAspectTag != null ? KIND_ESSENTIA : KIND_ITEM;
        String key = amountKind == KIND_FLUID ? fluidKeyOf(fluidStack)
            : amountKind == KIND_ESSENTIA ? essentiaKeyOf(essentiaAspectTag) : itemKeyOf(stack);
        Entry entry = CACHE.get(key);
        if (entry == null) {
            entry = new Entry();
            CACHE.put(key, entry);
        }
        entry.amountKind = amountKind;
        entry.lastAccess = now;
        entry.hits++;
        cleanupClientCaches(now);

        if (entry.hasResponse && now - entry.responseTime <= ttlFor(entry)) {
            addResponseLine(tooltip, entry);
            return;
        }

        if (!entry.pending || now - entry.requestTime > REQUEST_RETRY_MS) {
            requestAmount(key, stack, fluidStack, essentiaAspectTag, amountKind, entry, now);
        }

        if (entry.hasResponse) {
            addResponseLine(tooltip, entry);
        } else {
            addCheckingLine(tooltip);
        }
    }

    public static void handleAmountResponse(int requestId, int status, long amount) {
        if (!TooltipFeatureConfig.isTooltipEnabled()) {
            return;
        }
        String key = REQUEST_KEYS.remove(requestId);
        if (key == null) {
            return;
        }

        Entry entry = CACHE.get(key);
        if (entry == null) {
            entry = new Entry();
            CACHE.put(key, entry);
        }
        entry.amountKind = amountKindOf(key);

        entry.pending = false;
        entry.requestTime = Minecraft.getSystemTime();
        if (status == SAe2AmountResponse.STATUS_THROTTLED) {
            return;
        }
        if (status == SAe2AmountResponse.STATUS_OUT_OF_RANGE) {
            entry.hasResponse = true;
            entry.status = status;
            entry.amount = 0L;
            entry.responseTime = entry.requestTime;
            ae2ContextStatus = status;
            return;
        }
        if (status == SAe2AmountResponse.STATUS_NO_SYSTEM) {
            resetAe2State();
            return;
        }
        if (status == SAe2AmountResponse.STATUS_ERROR && entry.hasResponse) {
            entry.responseTime = entry.requestTime;
            return;
        }
        if (status == SAe2AmountResponse.STATUS_NO_SYSTEM || status == SAe2AmountResponse.STATUS_ERROR) {
            return;
        }

        entry.hasResponse = true;
        entry.status = status;
        entry.amount = amount;
        entry.responseTime = entry.requestTime;
    }

    public static void setAe2ContextAvailable(boolean available) {
        setAe2ContextStatus(available ? SAe2AmountResponse.STATUS_OK : SAe2AmountResponse.STATUS_NO_SYSTEM);
    }

    public static void setAe2ContextStatus(int status) {
        ae2ContextStatus = TooltipFeatureConfig.isTooltipEnabled() ? status : SAe2AmountResponse.STATUS_NO_SYSTEM;
        if (ae2ContextStatus != SAe2AmountResponse.STATUS_OK) {
            clearRequestState();
        }
    }

    public static boolean isAe2ContextAvailable() {
        return TooltipFeatureConfig.isTooltipEnabled() && ae2ContextStatus == SAe2AmountResponse.STATUS_OK;
    }

    public static void resetAe2State() {
        ae2ContextStatus = SAe2AmountResponse.STATUS_NO_SYSTEM;
        clearRequestState();
    }

    private static void clearRequestState() {
        CACHE.clear();
        REQUEST_KEYS.clear();
        BATCH_QUEUE.clear();
    }

    private static void requestContextRefresh(long now, long interval) {
        if (now < nextTooltipContextRefreshTime) {
            return;
        }
        nextTooltipContextRefreshTime = now + interval;
        NetworkHandler.sendToServer(new CAe2ContextRefresh());
    }

    private static void requestAmount(String key, ItemStack stack, FluidStack fluidStack, String essentiaAspectTag,
        int amountKind, Entry entry, long now) {
        if (!TooltipFeatureConfig.isTooltipEnabled()) {
            return;
        }
        if (!tryConsumeRequestToken(now)) {
            return;
        }

        int requestId = nextRequestId++;
        if (nextRequestId <= 0) {
            nextRequestId = 1;
        }

        ItemStack requestStack = null;
        FluidStack requestFluidStack = null;
        if (fluidStack == null) {
            requestStack = stack.copy();
            requestStack.stackSize = SINGLE_STACK_SIZE;
        } else {
            requestFluidStack = fluidStack.copy();
            requestFluidStack.amount = SINGLE_FLUID_AMOUNT;
        }

        REQUEST_KEYS.put(requestId, key);
        entry.pending = true;
        entry.amountKind = amountKind;
        entry.requestTime = now;

        BATCH_QUEUE.put(key, new PendingRequest(requestId, requestStack, requestFluidStack, essentiaAspectTag));
    }

    private static void flushPendingRequests(long now) {
        if (!TooltipFeatureConfig.isTooltipEnabled()) {
            resetAe2State();
            return;
        }
        if (BATCH_QUEUE.isEmpty() || now < nextBatchFlushTime) {
            return;
        }

        nextBatchFlushTime = now + BATCH_FLUSH_MS;
        List<CAe2AmountBatchRequest.Entry> entries = new ArrayList<>(Math.min(BATCH_QUEUE.size(), MAX_BATCH_SIZE));
        Iterator<Map.Entry<String, PendingRequest>> iterator = BATCH_QUEUE.entrySet()
            .iterator();
        while (iterator.hasNext() && entries.size() < MAX_BATCH_SIZE) {
            PendingRequest request = iterator.next()
                .getValue();
            entries.add(
                new CAe2AmountBatchRequest.Entry(
                    request.requestId,
                    request.stack,
                    request.fluidStack,
                    request.essentiaAspectTag));
            iterator.remove();
        }

        if (!entries.isEmpty()) {
            NetworkHandler.sendToServer(new CAe2AmountBatchRequest(entries));
        }
    }

    private static void cleanupClientCaches(long now) {
        if (now < nextClientCacheCleanupTime) {
            return;
        }
        nextClientCacheCleanupTime = now + CLIENT_CACHE_CLEANUP_MS;

        if (CACHE.size() > MAX_CLIENT_CACHE_ENTRIES) {
            Iterator<Map.Entry<String, Entry>> iterator = CACHE.entrySet()
                .iterator();
            while (iterator.hasNext()) {
                Entry entry = iterator.next()
                    .getValue();
                if (!entry.pending && now - entry.lastAccess > CLIENT_CACHE_CLEANUP_MS) {
                    iterator.remove();
                }
            }
        }

        if (FLUID_CONTAINER_CACHE.size() > MAX_FLUID_CACHE_ENTRIES) {
            Iterator<Map.Entry<String, FluidEntry>> iterator = FLUID_CONTAINER_CACHE.entrySet()
                .iterator();
            while (iterator.hasNext()) {
                if (now - iterator.next()
                    .getValue().createdAt > FLUID_CONTAINER_TTL_MS) {
                    iterator.remove();
                }
            }
        }

        Iterator<Map.Entry<Integer, String>> requestIterator = REQUEST_KEYS.entrySet()
            .iterator();
        while (requestIterator.hasNext()) {
            String key = requestIterator.next()
                .getValue();
            Entry entry = CACHE.get(key);
            if (entry == null || now - entry.requestTime > REQUEST_TIMEOUT_MS) {
                requestIterator.remove();
                BATCH_QUEUE.remove(key);
                if (entry != null) {
                    entry.pending = false;
                }
            }
        }
    }

    private static boolean tryConsumeRequestToken(long now) {
        if (lastRequestRefillTime <= 0L) {
            lastRequestRefillTime = now;
        }

        long elapsed = now - lastRequestRefillTime;
        if (elapsed > 0L) {
            requestTokens = Math
                .min(CLIENT_REQUEST_BURST, requestTokens + elapsed * CLIENT_REQUESTS_PER_SECOND / MILLIS_PER_SECOND);
            lastRequestRefillTime = now;
        }

        if (requestTokens < REQUEST_TOKEN_COST) {
            return false;
        }

        requestTokens -= REQUEST_TOKEN_COST;
        return true;
    }

    private static void addAmountLine(List<String> tooltip, long amount, int amountKind) {
        tooltip.add("");
        tooltip.add(
            EnumChatFormatting.GRAY
                + StatCollector.translateToLocalFormatted("bogosorter.tooltip.amount_in_system", EnumChatFormatting.AQUA.toString()
                + ReadableNumberConverter.INSTANCE.toWideReadableForm(amount)
                + suffixFor(amountKind)
            )
        );
    }

    private static void addResponseLine(List<String> tooltip, Entry entry) {
        if (entry.status == SAe2AmountResponse.STATUS_OUT_OF_RANGE) {
            addOutOfRangeLine(tooltip);
            return;
        }
        addAmountLine(tooltip, entry.amount, entry.amountKind);
    }

    private static void addCheckingLine(List<String> tooltip) {
        tooltip.add("");
        tooltip.add(
            EnumChatFormatting.DARK_GRAY
                + StatCollector.translateToLocal(
                "bogosorter.tooltip.amount_checking")
            );
    }

    private static void addOutOfRangeLine(List<String> tooltip) {
        tooltip.add("");
        tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("bogosorter.tooltip.amount_out_of_range"));
    }

    private static long ttlFor(Entry entry) {
        if (entry.status == SAe2AmountResponse.STATUS_NO_SYSTEM
            || entry.status == SAe2AmountResponse.STATUS_OUT_OF_RANGE) {
            return NO_SYSTEM_TTL_MS;
        }
        if (entry.amount <= 0L || entry.status == SAe2AmountResponse.STATUS_ERROR) {
            return MISS_TTL_MS;
        }
        if (entry.hits >= HOT_CACHE_HIT_THRESHOLD) {
            return CACHE_TTL_MS * HOT_CACHE_TTL_MULTIPLIER;
        }
        return CACHE_TTL_MS;
    }

    private static String itemKeyOf(ItemStack stack) {
        String itemName = String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem()));
        String tag = stack.getTagCompound() == null ? ""
            : Integer.toHexString(
                stack.getTagCompound()
                    .toString()
                    .hashCode());
        return "item|" + itemName + '|' + stack.getItemDamage() + '|' + tag;
    }

    private static String fluidKeyOf(FluidStack fluidStack) {
        String fluidName = fluidStack.getFluid() == null ? "null"
            : fluidStack.getFluid()
                .getName();
        String tag = fluidStack.tag == null ? ""
            : Integer.toHexString(
                fluidStack.tag.toString()
                    .hashCode());
        return "fluid|" + fluidName + '|' + tag;
    }

    private static String essentiaKeyOf(String aspectTag) {
        return "essentia|" + aspectTag;
    }

    private static int amountKindOf(String key) {
        if (key != null && key.startsWith("fluid|")) {
            return KIND_FLUID;
        }
        if (key != null && key.startsWith("essentia|")) {
            return KIND_ESSENTIA;
        }
        return KIND_ITEM;
    }

    private static String suffixFor(int amountKind) {
        if (amountKind == KIND_FLUID) {
            return " mB";
        }
        return "";
    }

    private static FluidStack fluidOf(ItemStack stack, long now) {
        String containerKey = itemKeyOf(stack);
        FluidEntry cached = FLUID_CONTAINER_CACHE.get(containerKey);
        if (cached != null && now - cached.createdAt <= FLUID_CONTAINER_TTL_MS) {
            return cached.fluidStack == null ? null : cached.fluidStack.copy();
        }

        FluidEntry entry = new FluidEntry();
        entry.createdAt = now;
        try {
            FluidStack fluidStack = StackInfo.getFluid(stack);
            entry.fluidStack = fluidStack == null ? null : fluidStack.copy();
        } catch (Throwable ignored) {}

        FLUID_CONTAINER_CACHE.put(containerKey, entry);
        return entry.fluidStack == null ? null : entry.fluidStack.copy();
    }

    private static boolean isAe2TerminalGui(Object gui) {
        if (gui == null) {
            return false;
        }

        Class<?> current = gui.getClass();
        while (current != null) {
            if ("appeng.client.gui.implementations.GuiMEMonitorable".equals(current.getName())) {
                return true;
            }
            current = current.getSuperclass();
        }

        return false;
    }

    private static boolean isAe2TerminalContextGui(GuiContainer gui) {
        if (isAe2TerminalGui(gui)) {
            return true;
        }
        Object firstGui = getRelatedGui(gui, "firstGui");
        if (isAe2TerminalGui(firstGui)) {
            return true;
        }
        return isAe2TerminalGui(getRelatedGui(gui, "getFirstScreen"));
    }

    private static Object getRelatedGui(GuiContainer gui, String memberName) {
        try {
            if (memberName.startsWith("get")) {
                return RecipeTooltipHandler.invokeMethod(gui, memberName);
            }

            Field field = RecipeTooltipHandler.findField(gui.getClass(), memberName);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            return field.get(gui);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public static final class Ae2GuiWatcher {

        private Object lastGui;
        private long nextRefreshTime;

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            long now = Minecraft.getSystemTime();
            flushPendingRequests(now);
            if (!TooltipFeatureConfig.isTooltipEnabled()) {
                this.lastGui = null;
                return;
            }

            Minecraft mc = Minecraft.getMinecraft();
            Object gui = mc.currentScreen;
            if (!isAe2TerminalGui(gui)) {
                this.lastGui = null;
                return;
            }

            if (gui == this.lastGui && now < this.nextRefreshTime) {
                return;
            }

            this.lastGui = gui;
            this.nextRefreshTime = now + CONTEXT_REFRESH_INTERVAL_MS;
            CACHE.clear();
            REQUEST_KEYS.clear();
            requestContextRefresh(now, 0L);
        }
    }

    private static final class RecipeTooltipHandler implements IContainerTooltipHandler {

        @Override
        public List<String> handleTooltip(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
            if (currenttip.isEmpty()) {
                return currenttip;
            }

            appendForHoveredRecipeStack(gui, null, mousex, mousey, currenttip);
            return currenttip;
        }

        @Override
        public List<String> handleItemDisplayName(GuiContainer gui, ItemStack itemstack, List<String> currenttip) {
            return currenttip;
        }

        @Override
        public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, int mousex, int mousey,
            List<String> currenttip) {
            if (gui instanceof GuiRecipe) {
                if (!appendForHoveredRecipeStack(gui, itemstack, mousex, mousey, currenttip)) {
                    appendAmountTooltip(itemstack, currenttip, isAe2TerminalContextGui(gui));
                }
            } else if (!isAe2TerminalStorageHover(gui)) {
                appendAmountTooltip(itemstack, currenttip, isAe2TerminalContextGui(gui));
            }
            return currenttip;
        }

        private static boolean isAe2TerminalStorageHover(GuiContainer gui) {
            return isAe2TerminalGui(gui) && !isThaumicArcaneCraftingTerminal(gui)
                && getAe2VirtualSlotUnderMouse(gui) != null;
        }

        private static Object getAe2VirtualSlotUnderMouse(GuiContainer gui) {
            try {
                return invokeMethod(gui, "getVirtualMESlotUnderMouse");
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        private static boolean isThaumicArcaneCraftingTerminal(GuiContainer gui) {
            Class<?> current = gui.getClass();
            while (current != null) {
                if ("thaumicenergistics.client.gui.GuiArcaneCraftingTerminal".equals(current.getName())) {
                    return true;
                }
                current = current.getSuperclass();
            }
            return false;
        }

        @Override
        public Map<String, String> handleHotkeys(GuiContainer gui, int mousex, int mousey,
            Map<String, String> hotkeys) {
            return hotkeys;
        }

        private static boolean appendForHoveredRecipeStack(GuiContainer gui, ItemStack itemstack, int mousex,
            int mousey, List<String> currenttip) {
            if (!(gui instanceof GuiRecipe)) {
                return false;
            }

            NEIRecipeWidget recipeWidget = getRecipeWidget((GuiRecipe<?>) gui, mousex, mousey);
            if (recipeWidget == null) {
                return false;
            }

            PositionedStack hovered = recipeWidget.getPositionedStackMouseOver(mousex, mousey);
            if (hovered == null || hovered.item == null) {
                return false;
            }

            appendAmountTooltip(hovered.item, currenttip, isAe2TerminalContextGui(gui));
            return true;
        }

        private static NEIRecipeWidget getRecipeWidget(GuiRecipe<?> gui, int mousex, int mousey) {
            try {
                Field containerField = findField(gui.getClass(), "container");
                if (containerField == null) {
                    return null;
                }

                containerField.setAccessible(true);
                Object container = containerField.get(gui);
                if (!(container instanceof WidgetContainer)) {
                    return null;
                }

                Widget widget = ((WidgetContainer) container).getWidgetUnderMouse(mousex, mousey);
                return widget instanceof NEIRecipeWidget ? (NEIRecipeWidget) widget : null;
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        private static Field findField(Class<?> type, String fieldName) {
            Class<?> current = type;
            while (current != null) {
                try {
                    return current.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignored) {
                    current = current.getSuperclass();
                }
            }

            return null;
        }

        private static Object invokeMethod(Object instance, String methodName) throws ReflectiveOperationException {
            Class<?> current = instance.getClass();
            while (current != null) {
                try {
                    java.lang.reflect.Method method = current.getDeclaredMethod(methodName);
                    method.setAccessible(true);
                    return method.invoke(instance);
                } catch (NoSuchMethodException ignored) {
                    current = current.getSuperclass();
                }
            }

            throw new NoSuchMethodException(methodName);
        }
    }

    private static final class Entry {

        private boolean pending;
        private boolean hasResponse;
        private int status = SAe2AmountResponse.STATUS_OK;
        private int amountKind;
        private long amount;
        private long requestTime;
        private long responseTime;
        private long lastAccess;
        private int hits;
    }

    private static final class PendingRequest {

        private final int requestId;
        private final ItemStack stack;
        private final FluidStack fluidStack;
        private final String essentiaAspectTag;

        private PendingRequest(int requestId, ItemStack stack, FluidStack fluidStack, String essentiaAspectTag) {
            this.requestId = requestId;
            this.stack = stack;
            this.fluidStack = fluidStack;
            this.essentiaAspectTag = essentiaAspectTag;
        }
    }

    private static final class FluidEntry {

        private FluidStack fluidStack;
        private long createdAt;
    }
}
