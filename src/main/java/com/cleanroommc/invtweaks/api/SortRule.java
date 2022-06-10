package com.cleanroommc.invtweaks.api;

import net.minecraft.item.ItemStack;

import java.util.Comparator;

public class SortRule implements Comparator<ItemStack> {

    private int priority = 0;
    private final Comparator<ItemStack> comparator;

    public SortRule(Comparator<ItemStack> comparator) {
        this.comparator = comparator;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public SortRule withPrio(int prio) {
        setPriority(prio);
        return this;
    }

    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        return comparator.compare(o1, o2);
    }
}
