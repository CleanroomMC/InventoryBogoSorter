package com.cleanroommc.invtweaks.api;

import net.minecraft.item.ItemStack;

import java.util.Comparator;

public abstract class SortRule implements Comparator<ItemStack> {

    private int priority = 0;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
