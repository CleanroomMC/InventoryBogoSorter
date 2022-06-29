package com.cleanroommc.bogosorter.common;

import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortConfigChangeEvent extends Event {

    @Unmodifiable
    public final List<SortRule<ItemStack>> configuredItemSortRules;
    @Unmodifiable
    public final List<NbtSortRule> configuredNbtSortRules;

    public SortConfigChangeEvent(List<SortRule<ItemStack>> configuredItemSortRules, List<NbtSortRule> configuredNbtSortRules) {
        this.configuredItemSortRules = Collections.unmodifiableList(configuredItemSortRules);
        this.configuredNbtSortRules = Collections.unmodifiableList(configuredNbtSortRules);
    }

    public Comparator<ItemStack> getItemComparator() {
        return SortHandler.ITEM_COMPARATOR;
    }
}
