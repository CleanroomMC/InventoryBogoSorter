package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.common.sort.color.ItemColorHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.lang3.StringUtils;

public class ClientSortData {

    @SideOnly(Side.CLIENT)
    public static ClientSortData of(ItemStack itemStack, boolean getColor, boolean getName) {
        int color = getColor ? ItemColorHelper.getItemColorHue(itemStack) : 0;
        String name = getName ? itemStack.getDisplayName() : StringUtils.EMPTY;
        return new ClientSortData(color, name);
    }

    public static ClientSortData readFromPacket(PacketBuffer buf) {
        int color = buf.readVarInt();
        String name = buf.readString(64);
        ClientSortData sortData = new ClientSortData(color, name);
        for (int i = 0, n = buf.readVarInt(); i < n; i++) {
            sortData.getSlotNumbers().add(buf.readVarInt());
        }
        return sortData;
    }

    private final int color;
    private final String name;
    private final IntList slotNumbers = new IntArrayList();

    public ClientSortData(int color, String name) {
        this.color = color;
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public IntList getSlotNumbers() {
        return slotNumbers;
    }

    public String getName() {
        return name;
    }

    public void writeToPacket(PacketBuffer buf) {
        buf.writeVarInt(color);
        buf.writeString(name);
        buf.writeVarInt(slotNumbers.size());
        for (int i : slotNumbers) buf.writeVarInt(i);
    }
}
