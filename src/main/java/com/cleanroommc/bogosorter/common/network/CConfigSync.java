package com.cleanroommc.bogosorter.common.network;

import com.cleanroommc.bogosorter.common.config.PlayerConfig;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import io.netty.buffer.Unpooled;

import java.io.IOException;

public class CConfigSync implements IPacket {

    private PacketBuffer packet;

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        packet = new PacketBuffer(Unpooled.buffer());
        PlayerConfig.getClient().writePacket(packet);
        NetworkUtils.writePacketBuffer(buf, packet);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        packet = NetworkUtils.readPacketBuffer(buf);
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        PlayerConfig.get(handler.player).readPacket(packet);
        return null;
    }
}
