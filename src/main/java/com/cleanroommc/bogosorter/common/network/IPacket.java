package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.BogoSorter;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

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
            BogoSorter.LOGGER.warn("Rejected malformed {} packet", getClass().getName(), e);
            throw new IllegalArgumentException("Malformed packet " + getClass().getName(), e);
        }
    }

    @Override
    default void toBytes(ByteBuf buf) {
        try {
            encode(new PacketBuffer(buf));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode packet " + getClass().getName(), e);
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
