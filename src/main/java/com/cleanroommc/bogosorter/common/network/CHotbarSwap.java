package com.cleanroommc.bogosorter.common.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class CHotbarSwap implements IPacket {

    private int hotbarIndex;
    private int swapIndex;

    public CHotbarSwap() {
    }

    public CHotbarSwap(int hotbarIndex, int swapIndex) {
        this.hotbarIndex = hotbarIndex;
        this.swapIndex = swapIndex;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeVarInt(hotbarIndex);
        buf.writeVarInt(swapIndex);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.hotbarIndex = buf.readVarInt();
        this.swapIndex = buf.readVarInt();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        ItemStack hotbarItem = handler.player.inventory.mainInventory.get(this.hotbarIndex);
        ItemStack toSwapItem = handler.player.inventory.mainInventory.get(this.swapIndex);
        if (hotbarItem.equals(toSwapItem)) return null;
        handler.player.inventory.mainInventory.set(this.hotbarIndex, toSwapItem);
        handler.player.inventory.mainInventory.set(this.swapIndex, hotbarItem);
        return null;
    }
}
