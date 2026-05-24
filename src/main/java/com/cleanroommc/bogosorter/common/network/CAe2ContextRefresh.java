package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.common.config.TooltipFeatureConfig;

public class CAe2ContextRefresh implements IPacket {

    private static final long SERVER_REFRESH_THROTTLE_MS = 1000L;
    private static final long SERVER_REFRESH_CACHE_RETAIN_MS = 30000L;
    private static final int MAX_REFRESH_THROTTLE_ENTRIES = 256;
    private static final Map<String, Long> NEXT_REFRESH_TIMES = new HashMap<>();

    @Override
    public void encode(PacketBuffer buf) throws IOException {}

    @Override
    public void decode(PacketBuffer buf) throws IOException {}

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        if (handler != null && handler.playerEntity != null) {
            if (!TooltipFeatureConfig.isTooltipEnabled()) {
                return new SAe2ContextStatusResponse(false);
            }
            boolean available = CAe2AmountRequest.hasKnownAeContext(handler.playerEntity);
            if (tryAcquire(handler.playerEntity)) {
                available = CAe2AmountRequest.refreshPlayerAeContext(handler.playerEntity);
            }
            return new SAe2ContextStatusResponse(available);
        }
        return null;
    }

    private static boolean tryAcquire(EntityPlayerMP player) {
        long now = System.currentTimeMillis();
        cleanup(now);

        String key = player.getCommandSenderName() + '@' + player.getEntityWorld().provider.dimensionId;
        Long nextRefresh = NEXT_REFRESH_TIMES.get(key);
        if (nextRefresh != null && now < nextRefresh.longValue()) {
            return false;
        }

        NEXT_REFRESH_TIMES.put(key, now + SERVER_REFRESH_THROTTLE_MS);
        return true;
    }

    private static void cleanup(long now) {
        if (NEXT_REFRESH_TIMES.size() < MAX_REFRESH_THROTTLE_ENTRIES) {
            return;
        }

        Iterator<Map.Entry<String, Long>> iterator = NEXT_REFRESH_TIMES.entrySet()
            .iterator();
        while (iterator.hasNext()) {
            if (now > iterator.next()
                .getValue()
                .longValue() + SERVER_REFRESH_CACHE_RETAIN_MS) {
                iterator.remove();
            }
        }
    }
}
