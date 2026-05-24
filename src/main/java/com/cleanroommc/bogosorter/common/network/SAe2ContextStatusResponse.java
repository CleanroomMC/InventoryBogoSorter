package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.compat.nei.Ae2TooltipClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SAe2ContextStatusResponse implements IPacket {

    private int status;

    public SAe2ContextStatusResponse() {}

    public SAe2ContextStatusResponse(boolean available) {
        this(available ? SAe2AmountResponse.STATUS_OK : SAe2AmountResponse.STATUS_NO_SYSTEM);
    }

    public SAe2ContextStatusResponse(int status) {
        this.status = status;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(this.status);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.status = buf.readInt();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        Ae2TooltipClient.setAe2ContextStatus(this.status);
        return null;
    }
}
