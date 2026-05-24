package com.cleanroommc.bogosorter.common.network;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.cleanroommc.bogosorter.common.config.TooltipFeatureConfig;
import com.cleanroommc.bogosorter.common.dropoff.InventoryData;
import com.cleanroommc.bogosorter.common.dropoff.InventoryManager;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCubeTarget;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;

public class CSearchAe2Terminal implements IPacket {

    private static final Color INVENTORY_HIGHLIGHT = Color.RED;
    private static final Color AE2_HIGHLIGHT = Color.CYAN;
    private static final long AE_SEARCH_CACHE_MS = 3000L;
    private static final long AE_SEARCH_MISS_CACHE_MS = 1500L;
    private static final long SEARCH_THROTTLE_MS = 250L;
    private static final long SEARCH_CACHE_RETAIN_MS = 30000L;
    private static final int SEARCH_CACHE_MAX_ENTRIES = 1024;
    private static final int SEARCH_THROTTLE_MAX_ENTRIES = 256;
    private static final int MAX_RENDER_TARGETS = 256;
    private static final int CHUNK_COORD_SHIFT = 4;
    private static final int NO_TARGET_COORD = 0;
    private static final Map<String, CachedTerminalTarget> AE_SEARCH_CACHE = new HashMap<>();
    private static final Map<String, Long> NEXT_SEARCH_TIMES = new HashMap<>();

    private ItemStack stack;

    public CSearchAe2Terminal() {}

    public CSearchAe2Terminal(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeItemStackToBuffer(this.stack);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.stack = buf.readItemStackFromBuffer();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        if (this.stack == null || handler == null || handler.playerEntity == null) {
            return null;
        }
        if (!TooltipFeatureConfig.isTooltipEnabled()) {
            return null;
        }

        try {
            EntityPlayerMP player = handler.playerEntity;
            if (!tryAcquireSearch(player)) {
                return null;
            }
            if (!CAe2AmountRequest.hasKnownAeContext(player)) {
                return null;
            }

            long now = System.currentTimeMillis();
            cleanupSearchCaches(now);
            CAe2AmountRequest.refreshPlayerAeContext(player);

            List<RendererCubeTarget> targets = findInventoryTargets(player, this.stack);
            TerminalTarget target = getCachedTerminalTarget(player, this.stack, now);
            if (target != null) {
                targets.add(new RendererCubeTarget(new BlockPos(target.x, target.y, target.z), AE2_HIGHLIGHT));
            }
            if (targets.isEmpty()) {
                return null;
            }

            boolean lookAtTerminal = target != null && targets.size() == 1;
            return new SSearchAe2TerminalResult(
                targets,
                lookAtTerminal,
                target == null ? NO_TARGET_COORD : target.x,
                target == null ? NO_TARGET_COORD : target.y,
                target == null ? NO_TARGET_COORD : target.z,
                target == null ? ForgeDirection.UNKNOWN.ordinal() : target.side.ordinal());
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static List<RendererCubeTarget> findInventoryTargets(EntityPlayerMP player, ItemStack stack) {
        List<RendererCubeTarget> targets = new ArrayList<>();
        InventoryManager inventoryManager = new InventoryManager(player);
        List<InventoryData> inventoryDataList = inventoryManager.getNearbyInventories();

        for (InventoryData inventoryData : inventoryDataList) {
            if (!inventoryContainsItem(inventoryData.getInventory(), stack)) {
                continue;
            }
            for (TileEntity entity : inventoryData.getEntities()) {
                targets.add(
                    new RendererCubeTarget(
                        new BlockPos(entity.xCoord, entity.yCoord, entity.zCoord),
                        INVENTORY_HIGHLIGHT));
                if (targets.size() >= MAX_RENDER_TARGETS) {
                    return targets;
                }
            }
        }

        return targets;
    }

    private static boolean inventoryContainsItem(IInventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (isSameItem(stackInSlot, stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSameItem(ItemStack left, ItemStack right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.getItem() != right.getItem() || left.getItemDamage() != right.getItemDamage()) {
            return false;
        }
        if (left.getTagCompound() == null) {
            return right.getTagCompound() == null;
        }
        return left.getTagCompound()
            .equals(right.getTagCompound());
    }

    private static boolean tryAcquireSearch(EntityPlayerMP player) {
        long now = System.currentTimeMillis();
        String key = playerKey(player);
        Long nextSearch = NEXT_SEARCH_TIMES.get(key);
        if (nextSearch != null && now < nextSearch.longValue()) {
            return false;
        }

        NEXT_SEARCH_TIMES.put(key, now + SEARCH_THROTTLE_MS);
        return true;
    }

    private static TerminalTarget getCachedTerminalTarget(EntityPlayerMP player, ItemStack stack, long now) {
        String key = searchCacheKey(player, stack);
        CachedTerminalTarget cached = AE_SEARCH_CACHE.get(key);
        if (cached != null && now < cached.expiresAt) {
            return cached.target;
        }

        TerminalTarget target = getContextSearchTarget(player, stack);
        AE_SEARCH_CACHE.put(
            key,
            new CachedTerminalTarget(target, now + (target == null ? AE_SEARCH_MISS_CACHE_MS : AE_SEARCH_CACHE_MS)));
        return target;
    }

    private static TerminalTarget getContextSearchTarget(EntityPlayerMP player, ItemStack stack) {
        CAe2AmountRequest.Ae2SearchTarget target = CAe2AmountRequest.getSearchTargetForStack(player, stack);
        if (target == null) {
            return null;
        }

        return new TerminalTarget(target.x, target.y, target.z, target.side);
    }

    private static String searchCacheKey(EntityPlayerMP player, ItemStack stack) {
        return playerKey(player) + '|'
            + (((int) player.posX) >> CHUNK_COORD_SHIFT)
            + '|'
            + (((int) player.posZ) >> CHUNK_COORD_SHIFT)
            + '|'
            + stackKeyOf(stack);
    }

    private static String playerKey(EntityPlayerMP player) {
        return player.getCommandSenderName() + '@' + player.getEntityWorld().provider.dimensionId;
    }

    private static String stackKeyOf(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return "null";
        }

        String itemName = String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem()));
        String tag = stack.getTagCompound() == null ? ""
            : Integer.toHexString(
                stack.getTagCompound()
                    .toString()
                    .hashCode());
        return itemName + '|' + stack.getItemDamage() + '|' + tag;
    }

    private static void cleanupSearchCaches(long now) {
        if (AE_SEARCH_CACHE.size() > SEARCH_CACHE_MAX_ENTRIES) {
            java.util.Iterator<Map.Entry<String, CachedTerminalTarget>> iterator = AE_SEARCH_CACHE.entrySet()
                .iterator();
            while (iterator.hasNext()) {
                if (now > iterator.next()
                    .getValue().expiresAt + SEARCH_CACHE_RETAIN_MS) {
                    iterator.remove();
                }
            }
        }

        if (NEXT_SEARCH_TIMES.size() > SEARCH_THROTTLE_MAX_ENTRIES) {
            java.util.Iterator<Map.Entry<String, Long>> iterator = NEXT_SEARCH_TIMES.entrySet()
                .iterator();
            while (iterator.hasNext()) {
                if (now > iterator.next()
                    .getValue()
                    .longValue() + SEARCH_CACHE_RETAIN_MS) {
                    iterator.remove();
                }
            }
        }
    }

    public static void clearSearchCaches() {
        AE_SEARCH_CACHE.clear();
        NEXT_SEARCH_TIMES.clear();
    }

    private static final class TerminalTarget {

        private final int x;
        private final int y;
        private final int z;
        private final ForgeDirection side;

        private TerminalTarget(int x, int y, int z, ForgeDirection side) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.side = side;
        }
    }

    private static final class CachedTerminalTarget {

        private final TerminalTarget target;
        private final long expiresAt;

        private CachedTerminalTarget(TerminalTarget target, long expiresAt) {
            this.target = target;
            this.expiresAt = expiresAt;
        }
    }
}
