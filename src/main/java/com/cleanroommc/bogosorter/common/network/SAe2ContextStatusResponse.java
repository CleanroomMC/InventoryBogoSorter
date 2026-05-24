package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.compat.nei.Ae2TooltipClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SAe2ContextStatusResponse implements IPacket {

    private boolean available;

    public SAe2ContextStatusResponse() {}

    public SAe2ContextStatusResponse(boolean available) {
        this.available = available;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeBoolean(this.available);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.available = buf.readBoolean();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        Ae2TooltipClient.setAe2ContextAvailable(this.available);
        return null;
    }
}
