package com.cleanroommc.bogosorter.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

/**
 * Joinked from Multiblocked
 */
public interface IPacket extends IMessage {

    void encode(PacketBuffer buf) throws IOException;

    void decode(PacketBuffer buf) throws IOException;

    @Override
    default void fromBytes(ByteBuf buf) {
        try {
            decode(new PacketBuffer(buf));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    default void toBytes(ByteBuf buf) {
        try {
            encode(new PacketBuffer(buf));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    default IPacket executeClient(NetHandlerPlayClient handler) {
        return null;
    }

    default IPacket executeServer(NetHandlerPlayServer handler) {
        return null;
    }
}
