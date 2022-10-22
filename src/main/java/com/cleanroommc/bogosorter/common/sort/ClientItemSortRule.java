package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.api.SortType;
import net.minecraft.item.ItemStack;

import java.util.Comparator;

public class ClientItemSortRule extends SortRule<ItemStack> {

    private Comparator<ItemSortContainer> comparator;

    public ClientItemSortRule(String key, SortType type, Comparator<ItemSortContainer> comparator) {
        super(key, type, null);
        this.comparator = comparator;
    }

    public int compareClient(ItemSortContainer o1, ItemSortContainer o2) {
        return isInverted() ? comparator.compare(o2, o1) : comparator.compare(o1, o2);
    }
}
