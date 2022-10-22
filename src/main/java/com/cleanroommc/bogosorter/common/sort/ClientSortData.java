package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.common.sort.color.ItemColorHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

public class ClientSortData {

    @SideOnly(Side.CLIENT)
    public static ClientSortData of(Slot slot, boolean getColor, boolean getName) {
        ItemStack itemStack = slot.getStack();
        int color = 0;
        String name = StringUtils.EMPTY;
        if (getColor) {
            color = ItemColorHelper.getItemColorHue(itemStack);
        }
        if (getName) {
            name = itemStack.getDisplayName();
        }
        return new ClientSortData(color, name, slot.slotNumber);
    }

    public static ClientSortData readFromPacket(PacketBuffer buf) {
        int slotNumber = buf.readVarInt();
        int color = buf.readVarInt();
        String name = buf.readString(64);
        return new ClientSortData(color, name, slotNumber);
    }

    private final int color;
    private final String name;
    private final int slotNumber;

    public ClientSortData(int color, String name, int slotNumber) {
        this.color = color;
        this.name = name;
        this.slotNumber = slotNumber;
    }

    public int getColor() {
        return color;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public String getName() {
        return name;
    }

    public void writeToPacket(PacketBuffer buf) {
        buf.writeVarInt(slotNumber);
        buf.writeVarInt(color);
        buf.writeString(name);
    }
}
