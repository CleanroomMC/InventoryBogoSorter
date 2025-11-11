package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.BogoSortAPI;

import net.minecraft.item.ItemStack;

import java.util.Objects;

public class ItemSortContainer {

    private final ItemStack itemStack;
    private final ClientSortData sortData;

    public ItemSortContainer(ItemStack itemStack, ClientSortData sortData) {
        this.itemStack = itemStack.copy();
        this.sortData = sortData;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ClientSortData getSortData() {
        return sortData;
    }

    public int getColorHue() {
        return sortData.getColor();
    }

    public String getName() {
        return sortData.getName();
    }

    public void shrink(int amount) {
        this.itemStack.shrink(amount);
    }

    public void grow(int amount) {
        this.itemStack.grow(amount);
    }

    public int getAmount() {
        return this.itemStack.getCount();
    }

    public boolean canMakeStack() {
        return getAmount() > 0;
    }

    public ItemStack makeStack(int max) {
        return this.itemStack.splitStack(max);
    }

    @Override
    public int hashCode() {
        ItemStack o = itemStack;
        return Objects.hash(o.getItem(), o.getMetadata(), o.getTagCompound(), BogoSortAPI.getItemAccessor(o).getCapNBT());
    }

    @Override
    public boolean equals(Object b1) {
        ItemStack a = itemStack;
        if (a == b1) return true;
        if (a == null || b1 == null) return false;
        ItemStack b;
        if (b1 instanceof ItemStack) {
            b = (ItemStack) b1;
        } else if (b1 instanceof ItemSortContainer) {
            b = ((ItemSortContainer) b1).itemStack;
        } else {
            return false;
        }
        return (a.isEmpty() && b.isEmpty()) ||
                (a.getItem() == b.getItem() &&
                        a.getMetadata() == b.getMetadata() &&
                        Objects.equals(a.getTagCompound(), b.getTagCompound()) &&
                        Objects.equals(BogoSortAPI.getItemAccessor(a).getCapNBT(), BogoSortAPI.getItemAccessor(b).getCapNBT()));
    }
}
