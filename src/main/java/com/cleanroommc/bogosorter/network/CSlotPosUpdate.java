package com.cleanroommc.bogosorter.network;

import io.netty.buffer.Unpooled;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

public class CSlotPosUpdate implements IPacket {

    private PacketBuffer buffer;
    private Container container;

    public CSlotPosUpdate(Container container) {
        this.container = container;
    }

    public CSlotPosUpdate() {
    }

    @Override
    public void encode(PacketBuffer buf) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        NetworkUtils.writeSlotPos(buffer, container);
        NetworkUtils.writePacketBuffer(buf, buffer);
    }

    @Override
    public void decode(PacketBuffer buf) {
        buffer = NetworkUtils.readPacketBuffer(buf);
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        container = handler.player.openContainer;
        NetworkUtils.readSlotPos(buffer, container);
        return null;
    }
}
