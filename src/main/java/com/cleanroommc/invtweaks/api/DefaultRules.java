package com.cleanroommc.invtweaks.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

public class DefaultRules {

    public static final SortRule<ItemStack> MOD_NAME = new SortRule<>(SortType.MOD, ItemCompareHelper::compareMod);
    public static final SortRule<ItemStack> ID_NAME = new SortRule<>(SortType.ID, ItemCompareHelper::compareId);
    public static final SortRule<ItemStack> META = new SortRule<>(SortType.META, ItemCompareHelper::compareMeta);
    public static final SortRule<ItemStack> NBT_SIZE = new SortRule<>(SortType.NBT, ItemCompareHelper::compareNbtSize);
    public static final SortRule<ItemStack> NBT_HAS = new SortRule<>(SortType.NBT, ItemCompareHelper::compareHasNbt);
    public static final SortRule<ItemStack> NBT_VALUES = new SortRule<>(SortType.NBT, ItemCompareHelper::compareNbtValues);
    public static final SortRule<ItemStack> NBT_ALL_VALUES = new SortRule<>(SortType.NBT, ItemCompareHelper::compareNbtValues);
    public static final SortRule<ItemStack> COUNT = new SortRule<>(SortType.COUNT, ItemCompareHelper::compareCount);
    public static final SortRule<ItemStack> ORE_DICT = new SortRule<>(SortType.OREDICT, ItemCompareHelper::compareOreDict);

    public static final NbtSortRule POTION = new NbtSortRule("Potion", Constants.NBT.TAG_STRING, ItemCompareHelper::comparePotionId, nbtBase -> {
        String[] potion = ((NBTTagString) nbtBase).getString().split(":");
        return potion[potion.length - 1];
    });
}
