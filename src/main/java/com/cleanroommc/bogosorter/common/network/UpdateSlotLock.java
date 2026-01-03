package com.cleanroommc.bogosorter.common.network;

import com.cleanroommc.bogosorter.common.lock.LockSlotCapability;

import com.cleanroommc.bogosorter.common.lock.SlotLock;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class UpdateSlotLock implements IPacket {

    private long lockedSlots;

    public UpdateSlotLock() {}

    public UpdateSlotLock(long lockedSlots) {
        this.lockedSlots = lockedSlots;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeLong(lockedSlots);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        lockedSlots = buf.readLong();
    }

    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        SlotLock.getClientCap().setLockedSlots(lockedSlots);
        return null;
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        LockSlotCapability.getForPlayer(handler.player).setLockedSlots(lockedSlots);
        return null;
    }
}
