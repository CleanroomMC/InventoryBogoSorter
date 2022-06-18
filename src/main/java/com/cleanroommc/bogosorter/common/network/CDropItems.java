package com.cleanroommc.bogosorter.common.network;

import com.cleanroommc.bogosorter.common.McUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CDropItems implements IPacket {

    private final List<ItemStack> items;

    public CDropItems() {
        items = new ArrayList<>();
    }

    public CDropItems(List<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(items.size());
        items.forEach(buf::writeItemStack);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        for (int i = 0, n = buf.readVarInt(); i < n; i++) {
            items.add(buf.readItemStack());
        }
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        McUtils.giveItemsToPlayer(handler.player, items);
        return null;
    }
}
