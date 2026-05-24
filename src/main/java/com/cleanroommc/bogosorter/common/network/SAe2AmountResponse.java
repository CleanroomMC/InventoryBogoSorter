package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.compat.nei.Ae2TooltipClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SAe2AmountResponse implements IPacket {

    public static final int STATUS_OK = 0;
    public static final int STATUS_NO_SYSTEM = 1;
    public static final int STATUS_THROTTLED = 2;
    public static final int STATUS_ERROR = 3;

    private int requestId;
    private int status;
    private long amount;

    public SAe2AmountResponse() {}

    public SAe2AmountResponse(int requestId, int status, long amount) {
        this.requestId = requestId;
        this.status = status;
        this.amount = amount;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(this.requestId);
        buf.writeInt(this.status);
        buf.writeLong(this.amount);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.requestId = buf.readInt();
        this.status = buf.readInt();
        this.amount = buf.readLong();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        Ae2TooltipClient.handleAmountResponse(this.requestId, this.status, this.amount);
        return null;
    }
}
