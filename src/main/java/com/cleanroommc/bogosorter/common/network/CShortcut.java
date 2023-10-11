package com.cleanroommc.bogosorter.common.network;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.ShortcutHandler;
import com.cleanroommc.bogosorter.api.ISlot;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class CShortcut implements IPacket {

    private Type type;
    private int slotNumber;

    public CShortcut(Type type, int slotNumber) {
        this.type = type;
        this.slotNumber = slotNumber;
    }

    public CShortcut() {
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(type);
        buf.writeVarInt(slotNumber);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        type = buf.readEnumValue(Type.class);
        slotNumber = buf.readVarInt();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        Container container = handler.player.openContainer;
        if (container == null) throw new IllegalStateException("Expected open container on server");
        ISlot slot = BogoSortAPI.getSlot(container, slotNumber);//container.getSlot(slotNumber);
        if (!slot.bogo$canTakeStack(handler.player)) {
            return null;
        }
        switch (type) {
            case MOVE_ALL:
                ShortcutHandler.moveAllItems(handler.player, container, slot, false);
                break;
            case MOVE_ALL_SAME:
                ShortcutHandler.moveAllItems(handler.player, container, slot, true);
                break;
            case MOVE_SINGLE:
                ShortcutHandler.moveSingleItem(handler.player, container, slot, false);
                break;
            case MOVE_SINGLE_EMPTY:
                ShortcutHandler.moveSingleItem(handler.player, container, slot, true);
                break;
            case DROP_ALL:
                ShortcutHandler.dropItems(handler.player, container, slot, false);
                break;
            case DROP_ALL_SAME:
                ShortcutHandler.dropItems(handler.player, container, slot, true);
                break;
        }
        container.detectAndSendChanges();
        return null;
    }

    public enum Type {
        MOVE_ALL, MOVE_ALL_SAME, MOVE_SINGLE, MOVE_SINGLE_EMPTY, DROP_ALL, DROP_ALL_SAME
    }
}
