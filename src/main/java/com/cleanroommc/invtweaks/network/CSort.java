package com.cleanroommc.invtweaks.network;

import com.cleanroommc.invtweaks.api.ISortableContainer;
import com.cleanroommc.invtweaks.sort.SortHandler;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

public class CSort implements IPacket {

    private int slot;

    public CSort(Slot slot) {
        this.slot = slot.slotNumber;
    }

    public CSort() {
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(slot);
    }

    @Override
    public void decode(PacketBuffer buf) {
        slot = buf.readVarInt();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        Container container = handler.player.openContainer;
        if (!(container instanceof ISortableContainer)) return null;
        SortHandler sortHandler = new SortHandler(container, (ISortableContainer) container);
        sortHandler.sort(slot);
        return null;
    }
}
