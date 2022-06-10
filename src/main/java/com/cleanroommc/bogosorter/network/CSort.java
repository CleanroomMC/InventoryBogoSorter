package com.cleanroommc.bogosorter.network;

import com.cleanroommc.bogosorter.api.InventoryTweaksAPI;
import com.cleanroommc.bogosorter.sort.SortHandler;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

public class CSort implements IPacket {

    private int slot;
    private boolean player;

    public CSort(Slot slot, boolean player) {
        this.slot = slot.slotNumber;
        this.player = player;
    }

    public CSort() {
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(slot);
        buf.writeBoolean(player);
    }

    @Override
    public void decode(PacketBuffer buf) {
        slot = buf.readVarInt();
        player = buf.readBoolean();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        Container container = handler.player.openContainer;
        if (!player && !InventoryTweaksAPI.isValidSortable(container)) return null;
        SortHandler sortHandler = new SortHandler(container, player);
        sortHandler.sort(slot);
        return null;
    }
}
