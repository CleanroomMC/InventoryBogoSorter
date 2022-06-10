package com.cleanroommc.invtweaks.sort;

import com.cleanroommc.invtweaks.InventoryTweaks;
import com.cleanroommc.invtweaks.api.DefaultRules;
import com.cleanroommc.invtweaks.api.ISortableContainer;
import com.cleanroommc.invtweaks.api.InventoryTweaksAPI;
import com.cleanroommc.invtweaks.api.SortRule;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.*;

public class SortHandler {

    private static final List<SortRule> sortRules = new ArrayList<>();

    static {
        sortRules.add(DefaultRules.MOD_NAME.withPrio(0));
        sortRules.add(DefaultRules.ID_NAME.withPrio(1));
        sortRules.add(DefaultRules.META.withPrio(2));
        sortRules.add(DefaultRules.NBT.withPrio(3));
    }

    public static void updateSortRules(Collection<SortRule> rules) {
        sortRules.clear();
        sortRules.addAll(rules);
        sortRules.sort(Comparator.comparingInt(SortRule::getPriority));
    }

    public static void updateSortRules(SortRule... rules) {
        updateSortRules(Arrays.asList(rules));
    }

    private final Container container;
    private GuiSortingContext context;

    public SortHandler(Container container) {
        this.container = container;
        createSortContext();
    }

    public void createSortContext() {
        if (container instanceof ISortableContainer) {
            GuiSortingContext.Builder builder = new GuiSortingContext.Builder(container);
            ((ISortableContainer) container).buildSortingContext(builder);
            this.context = builder.build();
            return;
        }
        if (InventoryTweaksAPI.isValidSortable(container)) {
            GuiSortingContext.Builder builder = new GuiSortingContext.Builder(container);
            InventoryTweaksAPI.getBuilder(container).accept(container, builder);
            this.context = builder.build();
        }
    }

    public void sort(int slotId) {
        InventoryTweaks.LOGGER.info("Sorting for slot {}", slotId);
        Slot[][] slotGroup = context.getSlotGroup(slotId);
        if (slotGroup != null) {
            sort(slotGroup);
        }
    }

    public void sort(Slot[][] slotGroup) {
        Object2IntMap<ItemStack> items = gatherItems(slotGroup);
        if (items.isEmpty()) return;
        LinkedList<ItemStack> itemList = new LinkedList<>(items.keySet());
        itemList.sort(ITEM_COMPARATOR);
        ItemStack item = itemList.pollFirst();
        if (item == null) return;
        int remaining = items.getInt(item);
        for (Slot[] slotRow : slotGroup) {
            for (Slot slot : slotRow) {
                if (item == ItemStack.EMPTY) {
                    slot.putStack(item);
                    continue;
                }
                if (!slot.isItemValid(item)) continue;
                int limit = Math.min(slot.getItemStackLimit(item), item.getMaxStackSize());
                limit = Math.min(remaining, limit);
                if (limit <= 0) continue;
                ItemStack toInsert = item.copy();
                toInsert.setCount(limit);
                slot.putStack(toInsert);
                remaining -= limit;
                if (remaining <= 0) {
                    if (itemList.isEmpty()) {
                        item = ItemStack.EMPTY;
                        continue;
                    }
                    item = itemList.pollFirst();
                    remaining = items.getInt(item);
                }
            }
        }
    }

    public Object2IntMap<ItemStack> gatherItems(Slot[][] slotGroup) {
        Object2IntOpenCustomHashMap<ItemStack> items = new Object2IntOpenCustomHashMap<>(ITEM_HASH_STRATEGY);
        for (Slot[] slotRow : slotGroup) {
            for (Slot slot : slotRow) {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    int amount = stack.getCount();
                    stack = stack.copy();
                    stack.setCount(1);
                    items.compute(stack, (key, value) -> value == null ? amount : value + amount);
                }
            }
        }
        return items;
    }

    public static final Comparator<ItemStack> ITEM_COMPARATOR = (stack1, stack2) -> {
        int result = 0;
        for (SortRule sortRule : sortRules) {
            result = sortRule.compare(stack1, stack2);
            if (result != 0) return result;
        }
        return result;
    };

    public static final Hash.Strategy<ItemStack> ITEM_HASH_STRATEGY = new Hash.Strategy<ItemStack>() {
        @Override
        public int hashCode(ItemStack o) {
            return Objects.hash(o.getItem(), o.getMetadata(), o.getTagCompound());
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == b) return true;
            if (a == null || b == null) return false;
            return (a.isEmpty() && b.isEmpty()) ||
                    (a.getItem() == b.getItem() &&
                            a.getMetadata() == b.getMetadata() &&
                            Objects.equals(a.getTagCompound(), b.getTagCompound()));
        }
    };
}
