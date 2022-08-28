package com.cleanroommc.bogosorter.common.network;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class CDropSlots implements IPacket {

    private final IntList slots;

    public CDropSlots(IntList slots) {
        this.slots = slots;
    }

    public CDropSlots() {
        this.slots = new IntArrayList();
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeVarInt(slots.size());
        for (int i : slots) {
            buf.writeVarInt(i);
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        for (int i = 0, n = buf.readVarInt(); i < n; i++) {
            slots.add(buf.readVarInt());
        }
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        Container container = handler.player.openContainer;
        if (container == null) throw new IllegalStateException("Expected open container on server");
        for (int slotId : slots) {
            Slot slot = container.getSlot(slotId);
            if (!slot.getStack().isEmpty()) {
                handler.player.dropItem(slot.getStack(), true);
                slot.putStack(ItemStack.EMPTY);
            }
        }
        container.detectAndSendChanges();
        return null;
    }
}
