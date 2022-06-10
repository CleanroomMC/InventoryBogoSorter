package com.cleanroommc.invtweaks.api;

public class DefaultRules {

    public static final SortRule MOD_NAME = new SortRule(ItemCompareHelper::compareMod);
    public static final SortRule ID_NAME = new SortRule(ItemCompareHelper::compareId);
    public static final SortRule META = new SortRule(ItemCompareHelper::compareMeta);
    public static final SortRule NBT = new SortRule(ItemCompareHelper::compareNbt);

}
