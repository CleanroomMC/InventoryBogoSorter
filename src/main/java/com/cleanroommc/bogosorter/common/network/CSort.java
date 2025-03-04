package com.cleanroommc.bogosorter.common.network;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.sort.ClientSortData;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CSort implements IPacket {

    private Collection<ClientSortData> clientSortDataList;
    private List<SortRule<ItemStack>> sortRules;
    private List<NbtSortRule> nbtSortRules;
    private int hover;
    private boolean player;

    public CSort(Collection<ClientSortData> clientSortDataList, List<SortRule<ItemStack>> sortRules, List<NbtSortRule> nbtSortRules, int hover, boolean player) {
        this.clientSortDataList = clientSortDataList;
        this.sortRules = sortRules;
        this.nbtSortRules = nbtSortRules;
        this.hover = hover;
        this.player = player;
    }

    public CSort() {
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeVarInt(hover);
        buf.writeBoolean(player);
        buf.writeVarInt(clientSortDataList.size());
        for (ClientSortData sortData : clientSortDataList) {
            sortData.writeToPacket(buf);
        }
        buf.writeVarInt(sortRules.size());
        for (SortRule<ItemStack> sortRule : sortRules) {
            buf.writeVarInt(sortRule.getSyncId());
            buf.writeBoolean(sortRule.isInverted());
        }
        buf.writeVarInt(nbtSortRules.size());
        for (NbtSortRule sortRule : nbtSortRules) {
            buf.writeVarInt(sortRule.getSyncId());
            buf.writeBoolean(sortRule.isInverted());
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        hover = buf.readVarInt();
        player = buf.readBoolean();
        clientSortDataList = new ArrayList<>();
        for (int i = 0, n = buf.readVarInt(); i < n; i++) {
            clientSortDataList.add(ClientSortData.readFromPacket(buf));
        }
        sortRules = new ArrayList<>();
        for (int i = 0, n = buf.readVarInt(); i < n; i++) {
            SortRule<ItemStack> sortRule = BogoSortAPI.INSTANCE.getItemSortRule(buf.readVarInt());
            sortRule.setInverted(buf.readBoolean());
            sortRules.add(sortRule);
        }
        nbtSortRules = new ArrayList<>();
        for (int i = 0, n = buf.readVarInt(); i < n; i++) {
            NbtSortRule sortRule = BogoSortAPI.INSTANCE.getNbtSortRule(buf.readVarInt());
            sortRule.setInverted(buf.readBoolean());
            nbtSortRules.add(sortRule);
        }
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        SortHandler.cacheItemSortRules.put(handler.player, sortRules);
        SortHandler.cacheNbtSortRules.put(handler.player, nbtSortRules);
        Int2ObjectOpenHashMap<ClientSortData> map = new Int2ObjectOpenHashMap<>();
        for (ClientSortData sortData : clientSortDataList) {
            for (int i : sortData.getSlotNumbers()) {
                map.put(i, sortData);
            }
        }
        SortHandler sortHandler = new SortHandler(handler.player, handler.player.openContainer, map);
        sortHandler.sort(hover);
        return null;
    }
}
