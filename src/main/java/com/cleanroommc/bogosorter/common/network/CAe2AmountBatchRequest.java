package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.bogosorter.common.config.TooltipFeatureConfig;

public class CAe2AmountBatchRequest implements IPacket {

    private static final int MAX_BATCH_SIZE = 32;
    private static final int MAX_ASPECT_TAG_LENGTH = 64;
    private static final long EMPTY_AMOUNT = 0L;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FLUID = 1;
    private static final int TYPE_ESSENTIA = 2;

    private final List<Entry> entries = new ArrayList<>();

    @SuppressWarnings("unused")
    public CAe2AmountBatchRequest() {}

    public CAe2AmountBatchRequest(List<Entry> entries) {
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
            if (entry.fluidStack != null) {
                buf.writeByte(TYPE_FLUID);
                NetworkUtils.writeFluidStack(buf, entry.fluidStack);
            } else if (entry.essentiaAspectTag != null) {
                buf.writeByte(TYPE_ESSENTIA);
                buf.writeStringToBuffer(entry.essentiaAspectTag);
            } else {
                buf.writeByte(TYPE_ITEM);
                buf.writeItemStackToBuffer(entry.stack);
            }
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.entries.clear();
        int count = Math.min(buf.readVarIntFromBuffer(), MAX_BATCH_SIZE);
        for (int i = 0; i < count; i++) {
            int requestId = buf.readInt();
            int type = buf.readByte();
            if (type == TYPE_FLUID) {
                this.entries.add(new Entry(requestId, null, NetworkUtils.readFluidStack(buf), null));
            } else if (type == TYPE_ESSENTIA) {
                this.entries.add(new Entry(requestId, null, null, buf.readStringFromBuffer(MAX_ASPECT_TAG_LENGTH)));
            } else {
                this.entries.add(new Entry(requestId, buf.readItemStackFromBuffer(), null, null));
            }
        }
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        if (handler == null || handler.playerEntity == null || this.entries.isEmpty()) {
            return null;
        }

        EntityPlayerMP player = handler.playerEntity;
        long now = System.currentTimeMillis();
        List<SAe2AmountBatchResponse.Entry> responses = new ArrayList<>(this.entries.size());
        if (!TooltipFeatureConfig.isTooltipEnabled()) {
            addResponses(responses, SAe2AmountResponse.STATUS_NO_SYSTEM);
            return new SAe2AmountBatchResponse(responses);
        }
        if (CAe2AmountRequest.arePlayerRequestsLimited(player, now, this.entries.size())) {
            addResponses(responses, SAe2AmountResponse.STATUS_THROTTLED);
            return new SAe2AmountBatchResponse(responses);
        }

        for (Entry entry : this.entries) {
            if (entry.stack == null && entry.fluidStack == null && entry.essentiaAspectTag == null) {
                responses.add(
                    new SAe2AmountBatchResponse.Entry(entry.requestId, SAe2AmountResponse.STATUS_ERROR, EMPTY_AMOUNT));
                continue;
            }

            CAe2AmountRequest.AmountLookupResult result = CAe2AmountRequest
                .lookupAmount(player, entry.stack, entry.fluidStack, entry.essentiaAspectTag, null, now);
            responses.add(new SAe2AmountBatchResponse.Entry(entry.requestId, result.status, result.amount));
        }

        return new SAe2AmountBatchResponse(responses);
    }

    private void addResponses(List<SAe2AmountBatchResponse.Entry> responses, int status) {
        for (Entry entry : this.entries) {
            responses.add(new SAe2AmountBatchResponse.Entry(entry.requestId, status, EMPTY_AMOUNT));
        }
    }

    public static final class Entry {

        private final int requestId;
        private final ItemStack stack;
        private final FluidStack fluidStack;
        private final String essentiaAspectTag;

        public Entry(int requestId, ItemStack stack, FluidStack fluidStack, String essentiaAspectTag) {
            this.requestId = requestId;
            this.stack = stack;
            this.fluidStack = fluidStack;
            this.essentiaAspectTag = essentiaAspectTag == null || essentiaAspectTag.isEmpty() ? null
                : essentiaAspectTag;
        }
    }
}
