package com.cleanroommc.bogosorter.common.network;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.network.PacketBuffer;

import org.junit.Test;

import com.cleanroommc.bogosorter.common.network.ae2.Ae2Status;
import com.cleanroommc.bogosorter.common.network.ae2.CAe2AmountBatchRequest;
import com.cleanroommc.bogosorter.common.network.ae2.SAe2AmountBatchResponse;

import io.netty.buffer.Unpooled;

public class Ae2BatchPacketValidationTest {

    @Test
    public void statusRangeIsStrict() {
        assertTrue(Ae2Status.isValid(Ae2Status.OK));
        assertTrue(Ae2Status.isValid(Ae2Status.UNSUPPORTED));
        assertFalse(Ae2Status.isValid(-1));
        assertFalse(Ae2Status.isValid(Ae2Status.UNSUPPORTED + 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void responseRejectsInvalidContextStatus() {
        new SAe2AmountBatchResponse(99, Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void responseEntryRejectsInvalidStatus() {
        new SAe2AmountBatchResponse.Entry(1, 99, 0L, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void responseEntryRejectsInvalidRetryDelay() {
        new SAe2AmountBatchResponse.Entry(1, Ae2Status.OK, 0L, 60001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void responseRejectsOversizedBatch() {
        List<SAe2AmountBatchResponse.Entry> entries = new ArrayList<>();
        for (int i = 0; i < 33; i++) {
            entries.add(new SAe2AmountBatchResponse.Entry(i, Ae2Status.OK, 0L, 0));
        }
        new SAe2AmountBatchResponse(Ae2Status.OK, entries);
    }

    @Test(expected = IOException.class)
    public void requestDecodeRejectsOversizedBatch() throws IOException {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarIntToBuffer(33);
        new CAe2AmountBatchRequest().decode(buffer);
    }

    @Test(expected = IOException.class)
    public void requestDecodeRejectsUnknownPayloadType() throws IOException {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarIntToBuffer(1);
        buffer.writeInt(1);
        buffer.writeByte(99);
        new CAe2AmountBatchRequest().decode(buffer);
    }
}
