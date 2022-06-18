package com.cleanroommc.bogosorter.common.network;

import com.cleanroommc.bogosorter.BogoSorter;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class SReloadConfig implements IPacket {

    @Override
    public void encode(PacketBuffer buf) throws IOException {
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
    }

    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        BogoSorter.SERIALIZER.loadConfig();
        return null;
    }
}
