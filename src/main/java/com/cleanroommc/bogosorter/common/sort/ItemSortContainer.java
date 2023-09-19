package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.BogoSortAPI;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class ItemSortContainer {

    private final ItemStack itemStack;
    private final ClientSortData sortData;
    private int amount;

    public ItemSortContainer(ItemStack itemStack, ClientSortData sortData) {
        this.itemStack = itemStack;
        this.sortData = sortData;
        this.amount = 0;
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
        this.amount -= amount;
    }

    public void grow(int amount) {
        this.amount += amount;
    }

    public int getAmount() {
        return amount;
    }

    public boolean canMakeStack() {
        return amount > 0;
    }

    public ItemStack makeStack(int max) {
        ItemStack copy = itemStack.copy();
        int size = Math.min(max, amount);
        copy.setCount(size);
        shrink(size);
        return copy;
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
