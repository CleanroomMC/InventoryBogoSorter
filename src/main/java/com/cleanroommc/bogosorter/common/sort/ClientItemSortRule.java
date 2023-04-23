package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.api.SortType;
import net.minecraft.item.ItemStack;

import java.util.Comparator;

public class ClientItemSortRule extends SortRule<ItemStack> {

    private final Comparator<ItemSortContainer> serverComparator;

    public ClientItemSortRule(String key, SortType type, Comparator<ItemStack> comparator, Comparator<ItemSortContainer> serverComparator) {
        super(key, type, comparator);
        this.serverComparator = serverComparator;
    }

    public int compareServer(ItemSortContainer o1, ItemSortContainer o2) {
        return isInverted() ? serverComparator.compare(o2, o1) : serverComparator.compare(o1, o2);
    }
}
