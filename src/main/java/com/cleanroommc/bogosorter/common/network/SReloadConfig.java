package com.cleanroommc.bogosorter.common.network;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;

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
        MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent(SortHandler.getItemSortRules(), SortHandler.getNbtSortRules()));
        return null;
    }
}
