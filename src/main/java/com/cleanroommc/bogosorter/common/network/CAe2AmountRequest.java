package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.bogosorter.common.config.TooltipFeatureConfig;
import com.cleanroommc.bogosorter.compat.ThaumicEnergisticsHelper;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;

public class CAe2AmountRequest implements IPacket {

    private static final int PLAYER_REQUESTS_PER_SECOND = 10;
    private static final int PLAYER_REQUEST_BURST = 40;
    private static final int NETWORK_LOOKUPS_PER_SECOND = 50;
    private static final int NETWORK_LOOKUP_BURST = 50;
    private static final int DEGRADED_NETWORK_LOOKUPS_PER_SECOND = 15;
    private static final int DEGRADED_NETWORK_LOOKUP_BURST = 15;
    private static final long LAZY_LOOKUP_CACHE_MS = 3000L;
    private static final long ZERO_LOOKUP_CACHE_MS = 5000L;
    private static final long DEGRADED_LOOKUP_CACHE_MS = 10000L;
    private static final long CACHE_RETAIN_MS = 30000L;
    private static final long CACHE_CLEANUP_INTERVAL_MS = 30000L;
    private static final long HIGH_LOAD_TICK_TIME_NS = 75000000L;
    private static final long SERVER_LOAD_CACHE_MS = 5000L;
    private static final double WIRELESS_POWER_CHECK_AMOUNT = 1.0D;
    private static final int MAX_LOOKUP_CACHE_ENTRIES = 8192;
    private static final int WIRELESS_CONTEXT_X = 0;
    private static final int WIRELESS_CONTEXT_Y = 0;
    private static final int WIRELESS_CONTEXT_Z = 0;
    private static final String OPEN_TERMINAL_CONTEXT_PREFIX = "open|";
    private static final long EMPTY_AMOUNT = 0L;
    private static final int MAX_ASPECT_TAG_LENGTH = 64;
    private static final int HOT_CACHE_HIT_THRESHOLD = 10;
    private static final long HOT_CACHE_TTL_MULTIPLIER = 2L;
    private static final int LOOKUP_EVICTION_BATCH_SIZE = 256;
    private static final int SINGLE_REQUEST = 1;
    private static final double MILLIS_PER_SECOND = 1000.0D;
    private static final double RATE_LIMIT_TOKEN_COST = 1.0D;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FLUID = 1;
    private static final int TYPE_ESSENTIA = 2;
    private static final String WIRELESS_ACCESS_POINT_CLASS = "appeng.tile.networking.TileWireless";
    private static final String AE2FC_BASE_CONTAINER_CLASS = "com.glodblock.github.client.gui.container.base.FCBaseContainer";

    private static final Map<String, RateLimit> PLAYER_LIMITS = new HashMap<>();
    private static final Map<String, RateLimit> NETWORK_LIMITS = new HashMap<>();
    private static final Map<String, LookupCacheEntry> LOOKUP_CACHE = new HashMap<>();
    private static long nextCleanupTime;
    private static long nextServerLoadCheckTime;
    private static boolean cachedServerUnderLoad;

    private int requestId;
    private ItemStack stack;
    private FluidStack fluidStack;
    private String essentiaAspectTag;

    public CAe2AmountRequest() {}

    public CAe2AmountRequest(int requestId, ItemStack stack, FluidStack fluidStack) {
        this(requestId, stack, fluidStack, null);
    }

    public CAe2AmountRequest(int requestId, ItemStack stack, FluidStack fluidStack, String essentiaAspectTag) {
        this.requestId = requestId;
        this.stack = stack;
        this.fluidStack = fluidStack;
        this.essentiaAspectTag = essentiaAspectTag == null || essentiaAspectTag.isEmpty() ? null : essentiaAspectTag;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(this.requestId);
        if (this.fluidStack != null) {
            buf.writeByte(TYPE_FLUID);
            NetworkUtils.writeFluidStack(buf, this.fluidStack);
        } else if (this.essentiaAspectTag != null) {
            buf.writeByte(TYPE_ESSENTIA);
            buf.writeStringToBuffer(this.essentiaAspectTag);
        } else {
            buf.writeByte(TYPE_ITEM);
            buf.writeItemStackToBuffer(this.stack);
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.requestId = buf.readInt();
        int type = buf.readByte();
        if (type == TYPE_FLUID) {
            this.fluidStack = NetworkUtils.readFluidStack(buf);
            this.stack = null;
            this.essentiaAspectTag = null;
        } else if (type == TYPE_ESSENTIA) {
            this.fluidStack = null;
            this.stack = null;
            this.essentiaAspectTag = buf.readStringFromBuffer(MAX_ASPECT_TAG_LENGTH);
        } else {
            this.stack = buf.readItemStackFromBuffer();
            this.fluidStack = null;
            this.essentiaAspectTag = null;
        }
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        if ((this.stack == null && this.fluidStack == null && this.essentiaAspectTag == null) || handler == null
            || handler.playerEntity == null) {
            return response(SAe2AmountResponse.STATUS_ERROR, EMPTY_AMOUNT);
        }

        long now = System.currentTimeMillis();
        cleanupCaches(now);

        EntityPlayerMP player = handler.playerEntity;
        if (!TooltipFeatureConfig.isTooltipEnabled()) {
            return response(SAe2AmountResponse.STATUS_NO_SYSTEM, EMPTY_AMOUNT);
        }
        if (!allowPlayerRequests(player, now, SINGLE_REQUEST)) {
            return response(SAe2AmountResponse.STATUS_THROTTLED, EMPTY_AMOUNT);
        }

        String lookupKey = this.essentiaAspectTag != null ? essentiaKeyOf(this.essentiaAspectTag)
            : this.fluidStack == null ? itemKeyOf(this.stack) : fluidKeyOf(this.fluidStack);
        if (lookupKey == null) {
            return response(SAe2AmountResponse.STATUS_ERROR, EMPTY_AMOUNT);
        }

        try {
            AmountLookupResult result = lookupAmount(
                player,
                this.stack,
                this.fluidStack,
                this.essentiaAspectTag,
                lookupKey,
                now);
            return response(result.status, result.amount);
        } catch (Throwable ignored) {
            return response(SAe2AmountResponse.STATUS_ERROR, EMPTY_AMOUNT);
        }
    }

    private SAe2AmountResponse response(int status, long amount) {
        return new SAe2AmountResponse(this.requestId, status, amount);
    }

    private static boolean rateLimit(Map<String, RateLimit> limits, String key, int perSecond, int burst, long now) {
        RateLimit limit = limits.get(key);
        if (limit == null) {
            limit = new RateLimit(perSecond, burst, now);
            limits.put(key, limit);
        }
        return limit.tryAcquire(now);
    }

    static boolean allowPlayerRequests(EntityPlayerMP player, long now, int count) {
        String key = playerKey(player);
        for (int i = 0; i < count; i++) {
            if (!rateLimit(PLAYER_LIMITS, key, PLAYER_REQUESTS_PER_SECOND, PLAYER_REQUEST_BURST, now)) {
                return false;
            }
        }
        return true;
    }

    static AmountLookupResult lookupAmount(EntityPlayerMP player, ItemStack stack, FluidStack fluidStack,
        String essentiaAspectTag, String lookupKey, long now) {
        if (!TooltipFeatureConfig.isTooltipEnabled()) {
            return AmountLookupResult.noSystem();
        }
        if (lookupKey == null) {
            lookupKey = essentiaAspectTag != null ? essentiaKeyOf(essentiaAspectTag)
                : fluidStack == null ? itemKeyOf(stack) : fluidKeyOf(fluidStack);
        }
        if (lookupKey == null) {
            return AmountLookupResult.error();
        }

        WirelessContextResult contextResult = getPlayerAeContext(player);
        if (contextResult.context == null || contextResult.context.host == null) {
            return AmountLookupResult.fromStatus(contextResult.status);
        }

        boolean degraded = isServerUnderLoad();
        String cacheKey = contextResult.context.networkKey + '|' + lookupKey;
        LookupCacheEntry cached = LOOKUP_CACHE.get(cacheKey);
        if (cached != null && now - cached.createdAt <= lookupCacheTtl(cached, degraded)) {
            cached.lastAccess = now;
            cached.hits++;
            return AmountLookupResult.ok(cached.amount);
        }

        if (degraded && cached != null && now - cached.createdAt <= CACHE_RETAIN_MS) {
            cached.lastAccess = now;
            cached.hits++;
            return AmountLookupResult.ok(cached.amount);
        }

        int perSecond = degraded ? DEGRADED_NETWORK_LOOKUPS_PER_SECOND : NETWORK_LOOKUPS_PER_SECOND;
        int burst = degraded ? DEGRADED_NETWORK_LOOKUP_BURST : NETWORK_LOOKUP_BURST;
        if (!rateLimit(NETWORK_LIMITS, contextResult.context.networkKey, perSecond, burst, now)) {
            if (cached != null) {
                cached.lastAccess = now;
                cached.hits++;
                return AmountLookupResult.ok(cached.amount);
            }
            return AmountLookupResult.throttled();
        }

        if (degraded && cached == null) {
            return AmountLookupResult.throttled();
        }

        long amount = essentiaAspectTag != null
            ? getTerminalEssentiaAmount(contextResult.context.host, essentiaAspectTag)
            : fluidStack == null ? getTerminalItemAmount(contextResult.context.host, stack)
                : getTerminalFluidAmount(contextResult.context.host, fluidStack);
        putLookupCache(cacheKey, amount, now);
        return AmountLookupResult.ok(amount);
    }

    private static long lookupCacheTtl(LookupCacheEntry entry, boolean degraded) {
        if (degraded) {
            return DEGRADED_LOOKUP_CACHE_MS;
        }
        if (entry.hits >= HOT_CACHE_HIT_THRESHOLD) {
            return LAZY_LOOKUP_CACHE_MS * HOT_CACHE_TTL_MULTIPLIER;
        }
        return entry.amount <= 0L ? ZERO_LOOKUP_CACHE_MS : LAZY_LOOKUP_CACHE_MS;
    }

    private static void putLookupCache(String cacheKey, long amount, long now) {
        if (LOOKUP_CACHE.size() >= MAX_LOOKUP_CACHE_ENTRIES) {
            evictColdLookupEntries(now);
        }
        LOOKUP_CACHE.put(cacheKey, new LookupCacheEntry(amount, now));
    }

    private static void evictColdLookupEntries(long now) {
        int removed = 0;
        for (Iterator<Map.Entry<String, LookupCacheEntry>> iterator = LOOKUP_CACHE.entrySet()
            .iterator(); iterator.hasNext();) {
            LookupCacheEntry entry = iterator.next()
                .getValue();
            if (now - entry.lastAccess > CACHE_RETAIN_MS || entry.hits <= 0) {
                iterator.remove();
                removed++;
                if (removed >= LOOKUP_EVICTION_BATCH_SIZE) {
                    return;
                }
            }
        }
    }

    public static void clearAe2Caches() {
        PLAYER_LIMITS.clear();
        NETWORK_LIMITS.clear();
        LOOKUP_CACHE.clear();
    }

    private static boolean isServerUnderLoad() {
        long now = System.currentTimeMillis();
        if (now < nextServerLoadCheckTime) {
            return cachedServerUnderLoad;
        }

        nextServerLoadCheckTime = now + SERVER_LOAD_CACHE_MS;
        try {
            MinecraftServer server = MinecraftServer.getServer();
            if (server == null || server.tickTimeArray == null || server.tickTimeArray.length == 0) {
                cachedServerUnderLoad = false;
                return cachedServerUnderLoad;
            }

            long total = 0L;
            for (long tickTime : server.tickTimeArray) {
                total += tickTime;
            }
            cachedServerUnderLoad = total / server.tickTimeArray.length >= HIGH_LOAD_TICK_TIME_NS;
            return cachedServerUnderLoad;
        } catch (Throwable ignored) {
            cachedServerUnderLoad = false;
            return cachedServerUnderLoad;
        }
    }

    private static WirelessContextResult getPlayerAeContext(EntityPlayerMP player) {
        WirelessContextResult openTerminalContext = getOpenTerminalContext(player);
        if (openTerminalContext.context != null) {
            return openTerminalContext;
        }
        return getWirelessTerminalContext(player);
    }

    static boolean refreshPlayerAeContext(EntityPlayerMP player) {
        return getWirelessContextStatus(player) == SAe2AmountResponse.STATUS_OK;
    }

    static boolean hasKnownAeContext(EntityPlayerMP player) {
        return TooltipFeatureConfig.isTooltipEnabled()
            && getWirelessContextStatus(player) == SAe2AmountResponse.STATUS_OK;
    }

    static int getWirelessContextStatus(EntityPlayerMP player) {
        if (player == null) {
            return SAe2AmountResponse.STATUS_NO_SYSTEM;
        }
        WirelessContextResult result = getWirelessTerminalContext(player);
        return result.status;
    }

    private static WirelessContextResult getOpenTerminalContext(EntityPlayerMP player) {
        if (player == null) {
            return WirelessContextResult.noSystem();
        }

        Container container = player.openContainer;
        if (container instanceof ContainerMEMonitorable monitorableContainer) {
            return getOpenAe2TerminalContext(player, monitorableContainer);
        }

        return getOpenAe2FcTerminalContext(player, container);
    }

    private static WirelessContextResult getOpenAe2TerminalContext(EntityPlayerMP player,
        ContainerMEMonitorable container) {
        if (!container.canInteractWith(player)) {
            return WirelessContextResult.noSystem();
        }

        Object target = container.getTarget();
        if (!(target instanceof ITerminalHost)) {
            return WirelessContextResult.noSystem();
        }

        ITerminalHost host = (ITerminalHost) target;
        IGridNode node = container.getNetworkNode();
        IGrid grid = node == null ? getGrid(host) : node.getGrid();
        String networkKey = grid == null ? OPEN_TERMINAL_CONTEXT_PREFIX + System.identityHashCode(host)
            : networkKeyOf(player.getEntityWorld(), grid);
        return WirelessContextResult.ok(
            new PlayerAeContext(
                player,
                host,
                WIRELESS_CONTEXT_X,
                WIRELESS_CONTEXT_Y,
                WIRELESS_CONTEXT_Z,
                ForgeDirection.UNKNOWN,
                networkKey));
    }

    private static WirelessContextResult getOpenAe2FcTerminalContext(EntityPlayerMP player, Container container) {
        if (container == null || !isInstanceOf(container, AE2FC_BASE_CONTAINER_CLASS)
            || !container.canInteractWith(player)) {
            return WirelessContextResult.noSystem();
        }

        try {
            Object host = invokeNoArg(container, "getHost");
            if (!(host instanceof ITerminalHost terminalHost)) {
                return WirelessContextResult.noSystem();
            }

            IGrid grid = getGrid(terminalHost);
            String networkKey = grid == null ? OPEN_TERMINAL_CONTEXT_PREFIX + System.identityHashCode(terminalHost)
                : networkKeyOf(player.getEntityWorld(), grid);
            return WirelessContextResult.ok(
                new PlayerAeContext(
                    player,
                    terminalHost,
                    WIRELESS_CONTEXT_X,
                    WIRELESS_CONTEXT_Y,
                    WIRELESS_CONTEXT_Z,
                    ForgeDirection.UNKNOWN,
                    networkKey));
        } catch (Throwable ignored) {
            return WirelessContextResult.noSystem();
        }
    }

    static Ae2SearchTarget getSearchTargetForStack(EntityPlayerMP player, ItemStack stack) {
        if (player == null || stack == null || stack.getItem() == null) {
            return null;
        }

        WirelessContextResult contextResult = getPlayerAeContext(player);
        PlayerAeContext context = contextResult.context;
        if (context == null || context.host == null) {
            return null;
        }

        long amount = getTerminalItemAmount(context.host, stack);
        if (amount <= 0L) {
            return null;
        }

        if (context.dimension != player.getEntityWorld().provider.dimensionId) {
            return null;
        }

        if (context.x == 0 && context.y == 0 && context.z == 0) {
            return null;
        }

        return new Ae2SearchTarget(
            context.x,
            context.y,
            context.z,
            context.side == null ? ForgeDirection.UNKNOWN : context.side,
            amount);
    }

    private static WirelessContextResult getWirelessTerminalContext(EntityPlayerMP player) {
        WirelessContextResult fallback = null;
        IInventory baubles = getBaublesInventory(player);
        if (baubles != null) {
            int slotCount = baubles.getSizeInventory();
            for (int slot = 0; slot < slotCount; slot++) {
                WirelessContextResult result = createWirelessTerminalContext(player, baubles.getStackInSlot(slot));
                if (result == null) {
                    continue;
                }
                if (result.context != null) {
                    return result;
                }
                fallback = preferredWirelessFailure(fallback, result);
            }
        }

        for (ItemStack inventoryStack : player.inventory.mainInventory) {
            WirelessContextResult result = createWirelessTerminalContext(player, inventoryStack);
            if (result == null) {
                continue;
            }
            if (result.context != null) {
                return result;
            }
            fallback = preferredWirelessFailure(fallback, result);
        }

        return fallback == null ? WirelessContextResult.noSystem() : fallback;
    }

    private static WirelessContextResult preferredWirelessFailure(WirelessContextResult current,
        WirelessContextResult candidate) {
        if (current == null || candidate.status == SAe2AmountResponse.STATUS_OUT_OF_RANGE) {
            return candidate;
        }
        return current;
    }

    private static WirelessContextResult createWirelessTerminalContext(EntityPlayerMP player,
        ItemStack wirelessTerminal) {
        try {
            if (!isWirelessTerminal(wirelessTerminal)) {
                return null;
            }

            IWirelessTermHandler wirelessHandler = getWirelessTerminalHandler(wirelessTerminal);
            if (wirelessHandler == null) {
                return WirelessContextResult.noSystem();
            }

            ILocatable locatable = getLinkedWirelessTerminalHost(wirelessHandler, wirelessTerminal);
            if (!(locatable instanceof IGridHost)) {
                return WirelessContextResult.noSystem();
            }

            IGridNode node = ((IGridHost) locatable).getGridNode(ForgeDirection.UNKNOWN);
            if (node == null) {
                return WirelessContextResult.noSystem();
            }

            IGrid grid = node.getGrid();
            if (grid == null || !Ae2AccessHelper.canPlayerUseGrid(player, grid, node)) {
                return WirelessContextResult.noSystem();
            }

            if (!isWirelessTerminalInRange(player, wirelessHandler, wirelessTerminal, grid)) {
                return WirelessContextResult.outOfRange();
            }

            if (!hasWirelessTerminalPower(player, wirelessHandler, wirelessTerminal)) {
                return WirelessContextResult.noSystem();
            }

            IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
            if (storageGrid == null) {
                return WirelessContextResult.noSystem();
            }

            return WirelessContextResult.ok(
                new PlayerAeContext(
                    player,
                    new StorageGridTerminalHost(storageGrid, wirelessHandler.getConfigManager(wirelessTerminal), grid),
                    WIRELESS_CONTEXT_X,
                    WIRELESS_CONTEXT_Y,
                    WIRELESS_CONTEXT_Z,
                    ForgeDirection.UNKNOWN,
                    networkKeyOf(player.getEntityWorld(), grid)));
        } catch (Throwable ignored) {
            return WirelessContextResult.noSystem();
        }
    }

    private static IWirelessTermHandler getWirelessTerminalHandler(ItemStack wirelessTerminal) {
        IWirelessTermHandler wirelessHandler = AEApi.instance()
            .registries()
            .wireless()
            .getWirelessTerminalHandler(wirelessTerminal);
        if (wirelessHandler == null || !wirelessHandler.canHandle(wirelessTerminal)) {
            return null;
        }
        return wirelessHandler;
    }

    private static ILocatable getLinkedWirelessTerminalHost(IWirelessTermHandler wirelessHandler,
        ItemStack wirelessTerminal) {
        String encryptionKey = wirelessHandler.getEncryptionKey(wirelessTerminal);
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            return null;
        }

        try {
            return AEApi.instance()
                .registries()
                .locatable()
                .getLocatableBy(Long.parseLong(encryptionKey));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static boolean hasWirelessTerminalPower(EntityPlayerMP player, IWirelessTermHandler wirelessHandler,
        ItemStack wirelessTerminal) {
        try {
            return wirelessHandler.hasInfinityPower(wirelessTerminal)
                || wirelessHandler.hasPower(player, WIRELESS_POWER_CHECK_AMOUNT, wirelessTerminal);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean isWirelessTerminalInRange(EntityPlayerMP player, IWirelessTermHandler wirelessHandler,
        ItemStack wirelessTerminal, IGrid grid) {
        try {
            if (wirelessHandler.hasInfinityRange(wirelessTerminal)) {
                return true;
            }

            Class<?> tileWireless = Class.forName(WIRELESS_ACCESS_POINT_CLASS);
            IMachineSet accessPoints = grid.getMachines((Class<? extends IGridHost>) tileWireless);
            for (IGridNode accessPointNode : accessPoints) {
                Object machine = accessPointNode.getMachine();
                if (machine instanceof IWirelessAccessPoint
                    && isWirelessAccessPointInRange(player, (IWirelessAccessPoint) machine)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }

    private static boolean isWirelessAccessPointInRange(EntityPlayerMP player, IWirelessAccessPoint accessPoint) {
        if (!accessPoint.isActive()) {
            return false;
        }

        DimensionalCoord location = accessPoint.getLocation();
        if (location == null || location.getWorld() != player.worldObj) {
            return false;
        }

        double range = accessPoint.getRange();
        double dx = location.x - player.posX;
        double dy = location.y - player.posY;
        double dz = location.z - player.posZ;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        return distanceSq < range * range;
    }

    private static IInventory getBaublesInventory(EntityPlayerMP player) {
        IInventory baubles = getBaublesInventoryFromPlayerHandler(player);
        return baubles == null ? getBaublesInventoryFromApi(player) : baubles;
    }

    private static IInventory getBaublesInventoryFromPlayerHandler(EntityPlayerMP player) {
        return getBaublesInventory("baubles.common.lib.PlayerHandler", "getPlayerBaubles", player);
    }

    private static IInventory getBaublesInventoryFromApi(EntityPlayerMP player) {
        return getBaublesInventory("baubles.api.BaublesApi", "getBaubles", player);
    }

    private static IInventory getBaublesInventory(String className, String methodName, EntityPlayerMP player) {
        try {
            Class<?> inventoryProvider = Class.forName(className);
            Method method = inventoryProvider.getMethod(methodName, EntityPlayer.class);
            Object inventory = method.invoke(null, player);
            return inventory instanceof IInventory ? (IInventory) inventory : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isWirelessTerminal(ItemStack stack) {
        try {
            return stack != null && AEApi.instance()
                .registries()
                .wireless()
                .isWirelessTerminal(stack);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isInstanceOf(Object instance, String className) {
        Class<?> current = instance.getClass();
        while (current != null) {
            if (className.equals(current.getName())) {
                return true;
            }
            current = current.getSuperclass();
        }
        return false;
    }

    private static Object invokeNoArg(Object instance, String methodName) throws ReflectiveOperationException {
        Class<?> current = instance.getClass();
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName);
                method.setAccessible(true);
                return method.invoke(instance);
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    private static String networkKeyOf(World world, IGrid grid) {
        return "grid|" + world.provider.dimensionId + '|' + System.identityHashCode(grid);
    }

    private static IGrid getGrid(ITerminalHost host) {
        if (host instanceof StorageGridTerminalHost) {
            return ((StorageGridTerminalHost) host).grid;
        }
        if (host instanceof IGridHost) {
            IGridNode node = ((IGridHost) host).getGridNode(ForgeDirection.UNKNOWN);
            return node == null ? null : node.getGrid();
        }
        if (host instanceof IActionHost) {
            IGridNode node = ((IActionHost) host).getActionableNode();
            return node == null ? null : node.getGrid();
        }
        return null;
    }

    private static long getTerminalItemAmount(ITerminalHost host, ItemStack stack) {
        IMEMonitor<IAEItemStack> itemInventory = host.getItemInventory();
        if (itemInventory == null || itemInventory.getStorageList() == null) {
            return EMPTY_AMOUNT;
        }

        IAEItemStack requested = AEItemStack.create(stack);
        if (requested == null) {
            return EMPTY_AMOUNT;
        }

        IAEItemStack found = itemInventory.getStorageList()
            .findPrecise(requested);
        return found == null ? EMPTY_AMOUNT : found.getStackSize();
    }

    private static long getTerminalFluidAmount(ITerminalHost host, FluidStack fluidStack) {
        IMEMonitor<IAEFluidStack> fluidInventory = host.getFluidInventory();
        if (fluidInventory == null || fluidInventory.getStorageList() == null) {
            return EMPTY_AMOUNT;
        }

        IAEFluidStack requested = AEFluidStack.create(fluidStack);
        if (requested == null) {
            return EMPTY_AMOUNT;
        }

        IAEFluidStack found = fluidInventory.getStorageList()
            .findPrecise(requested);
        return found == null ? EMPTY_AMOUNT : found.getStackSize();
    }

    private static long getTerminalEssentiaAmount(ITerminalHost host, String aspectTag) {
        IGrid grid = getGrid(host);
        if (grid == null) {
            return EMPTY_AMOUNT;
        }
        return ThaumicEnergisticsHelper.getEssentiaAmount(grid, aspectTag);
    }

    private static String playerKey(EntityPlayerMP player) {
        UUID uuid = player.getGameProfile() == null ? null
            : player.getGameProfile()
                .getId();
        return uuid == null ? player.getCommandSenderName() : uuid.toString();
    }

    private static String itemKeyOf(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return null;
        }
        String itemName = String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem()));
        String tag = stack.getTagCompound() == null ? ""
            : Integer.toHexString(
                stack.getTagCompound()
                    .toString()
                    .hashCode());
        return "item|" + itemName + '|' + stack.getItemDamage() + '|' + tag;
    }

    private static String fluidKeyOf(FluidStack fluidStack) {
        if (fluidStack == null || fluidStack.getFluid() == null) {
            return null;
        }
        String tag = fluidStack.tag == null ? ""
            : Integer.toHexString(
                fluidStack.tag.toString()
                    .hashCode());
        return "fluid|" + fluidStack.getFluid()
            .getName() + '|' + tag;
    }

    private static String essentiaKeyOf(String aspectTag) {
        return aspectTag == null || aspectTag.isEmpty() ? null : "essentia|" + aspectTag;
    }

    private static void cleanupCaches(long now) {
        if (now < nextCleanupTime) {
            return;
        }
        nextCleanupTime = now + CACHE_CLEANUP_INTERVAL_MS;

        for (Iterator<Map.Entry<String, LookupCacheEntry>> iterator = LOOKUP_CACHE.entrySet()
            .iterator(); iterator.hasNext();) {
            if (now - iterator.next()
                .getValue().createdAt > CACHE_RETAIN_MS) {
                iterator.remove();
            }
        }

        cleanupRateLimits(PLAYER_LIMITS, now);
        cleanupRateLimits(NETWORK_LIMITS, now);
    }

    private static void cleanupRateLimits(Map<String, RateLimit> limits, long now) {
        for (Iterator<Map.Entry<String, RateLimit>> iterator = limits.entrySet()
            .iterator(); iterator.hasNext();) {
            if (now - iterator.next()
                .getValue().lastRefillTime > CACHE_RETAIN_MS) {
                iterator.remove();
            }
        }
    }

    private static final class PlayerAeContext {

        private final int dimension;
        private final int x;
        private final int y;
        private final int z;
        private final ForgeDirection side;
        private ITerminalHost host;
        private String networkKey;

        private PlayerAeContext(EntityPlayerMP player, ITerminalHost host, int x, int y, int z, ForgeDirection side,
            String networkKey) {
            this(player.getEntityWorld().provider.dimensionId, host, x, y, z, side, networkKey);
        }

        private PlayerAeContext(int dimension, ITerminalHost host, int x, int y, int z, ForgeDirection side,
            String networkKey) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.side = side;
            this.host = host;
            this.networkKey = networkKey;
        }
    }

    private static final class WirelessContextResult {

        private final PlayerAeContext context;
        private final int status;

        private WirelessContextResult(PlayerAeContext context, int status) {
            this.context = context;
            this.status = status;
        }

        private static WirelessContextResult ok(PlayerAeContext context) {
            return new WirelessContextResult(context, SAe2AmountResponse.STATUS_OK);
        }

        private static WirelessContextResult noSystem() {
            return new WirelessContextResult(null, SAe2AmountResponse.STATUS_NO_SYSTEM);
        }

        private static WirelessContextResult outOfRange() {
            return new WirelessContextResult(null, SAe2AmountResponse.STATUS_OUT_OF_RANGE);
        }
    }

    private static final class StorageGridTerminalHost implements ITerminalHost {

        private final IStorageGrid storageGrid;
        private final IConfigManager configManager;
        private final IGrid grid;

        private StorageGridTerminalHost(IStorageGrid storageGrid, IConfigManager configManager, IGrid grid) {
            this.storageGrid = storageGrid;
            this.configManager = configManager;
            this.grid = grid;
        }

        @Override
        public IMEMonitor<IAEItemStack> getItemInventory() {
            return this.storageGrid.getItemInventory();
        }

        @Override
        public IMEMonitor<IAEFluidStack> getFluidInventory() {
            return this.storageGrid.getFluidInventory();
        }

        @Override
        public IConfigManager getConfigManager() {
            return this.configManager;
        }
    }

    private static final class LookupCacheEntry {

        private final long amount;
        private final long createdAt;
        private long lastAccess;
        private int hits;

        private LookupCacheEntry(long amount, long createdAt) {
            this.amount = amount;
            this.createdAt = createdAt;
            this.lastAccess = createdAt;
        }
    }

    static final class AmountLookupResult {

        final int status;
        final long amount;

        private AmountLookupResult(int status, long amount) {
            this.status = status;
            this.amount = amount;
        }

        static AmountLookupResult ok(long amount) {
            return new AmountLookupResult(SAe2AmountResponse.STATUS_OK, amount);
        }

        static AmountLookupResult noSystem() {
            return new AmountLookupResult(SAe2AmountResponse.STATUS_NO_SYSTEM, 0L);
        }

        static AmountLookupResult throttled() {
            return new AmountLookupResult(SAe2AmountResponse.STATUS_THROTTLED, 0L);
        }

        static AmountLookupResult error() {
            return new AmountLookupResult(SAe2AmountResponse.STATUS_ERROR, 0L);
        }

        static AmountLookupResult fromStatus(int status) {
            if (status == SAe2AmountResponse.STATUS_OUT_OF_RANGE) {
                return new AmountLookupResult(SAe2AmountResponse.STATUS_OUT_OF_RANGE, 0L);
            }
            return noSystem();
        }
    }

    static final class Ae2SearchTarget {

        final int x;
        final int y;
        final int z;
        final ForgeDirection side;
        final long amount;

        private Ae2SearchTarget(int x, int y, int z, ForgeDirection side, long amount) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.side = side;
            this.amount = amount;
        }
    }

    private static final class RateLimit {

        private final int perSecond;
        private final int burst;
        private double tokens;
        private long lastRefillTime;

        private RateLimit(int perSecond, int burst, long now) {
            this.perSecond = perSecond;
            this.burst = burst;
            this.tokens = burst;
            this.lastRefillTime = now;
        }

        private boolean tryAcquire(long now) {
            long elapsed = now - this.lastRefillTime;
            if (elapsed > 0L) {
                this.tokens = Math.min(this.burst, this.tokens + elapsed * this.perSecond / MILLIS_PER_SECOND);
                this.lastRefillTime = now;
            }

            if (this.tokens < RATE_LIMIT_TOKEN_COST) {
                return false;
            }

            this.tokens -= RATE_LIMIT_TOKEN_COST;
            return true;
        }
    }
}
