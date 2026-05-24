package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.compat.nei.Ae2TooltipClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SAe2AmountBatchResponse implements IPacket {

    private static final int MAX_BATCH_SIZE = 32;

    private final List<Entry> entries = new ArrayList<>();

    public SAe2AmountBatchResponse() {}

    public SAe2AmountBatchResponse(List<Entry> entries) {
        int count = Math.min(entries.size(), MAX_BATCH_SIZE);
        for (int i = 0; i < count; i++) {
            this.entries.add(entries.get(i));
        }
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.entries.size());
        for (Entry entry : this.entries) {
            buf.writeInt(entry.requestId);
            buf.writeInt(entry.status);
            buf.writeLong(entry.amount);
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.entries.clear();
        int count = Math.min(buf.readVarIntFromBuffer(), MAX_BATCH_SIZE);
        for (int i = 0; i < count; i++) {
            this.entries.add(new Entry(buf.readInt(), buf.readInt(), buf.readLong()));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        for (Entry entry : this.entries) {
            Ae2TooltipClient.handleAmountResponse(entry.requestId, entry.status, entry.amount);
        }
        return null;
    }

    public static final class Entry {

        private final int requestId;
        private final int status;
        private final long amount;

        public Entry(int requestId, int status, long amount) {
            this.requestId = requestId;
            this.status = status;
            this.amount = amount;
        }
    }
}
