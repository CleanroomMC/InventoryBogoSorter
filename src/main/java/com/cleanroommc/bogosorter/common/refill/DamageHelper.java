package com.cleanroommc.bogosorter.common.refill;

import net.minecraft.item.ItemStack;

public class DamageHelper {

    public static int getDamage(ItemStack item) {
        if (item.isEmpty()) return 0;
        /*if (BogoSorter.isAnyGtLoaded() && item.getItem() instanceof IToolItem) {
            return ((IToolItem) item.getItem()).getItemDamage(item);
        }*/
        return item.isItemStackDamageable() ? item.getItemDamage() : 0;
    }

    public static int getMaxDamage(ItemStack item) {
        if (item.isEmpty()) return 0;
        /*if (BogoSorter.isAnyGtLoaded() && item.getItem() instanceof IToolItem) {
            return ((IToolItem) item.getItem()).getMaxItemDamage(item);
        }*/
        return Math.max(item.getMaxDamage(), 0);
    }

    public static int getDurability(ItemStack item) {
        if (item.isEmpty()) return 0;
        /*if (BogoSorter.isAnyGtLoaded() && item.getItem() instanceof IToolItem) {
            IToolItem tool = (IToolItem) item.getItem();
            if (isUnbreakable(item)) {
                return tool.getMaxItemDamage(item);
            }
            return tool.getMaxItemDamage(item) - tool.getItemDamage(item);
        }*/
        if (item.getMaxDamage() <= 0) return 0;
        if (isUnbreakable(item)) {
            return item.getMaxDamage() + 1;
        }
        return item.getMaxDamage() - item.getItemDamage() + 1;
    }

    public static boolean isUnbreakable(ItemStack item) {
        return !item.isEmpty() && item.hasTagCompound() && item.getTagCompound().getBoolean("Unbreakable");
    }
}
