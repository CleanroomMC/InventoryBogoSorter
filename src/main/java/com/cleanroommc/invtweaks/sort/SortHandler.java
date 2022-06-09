package com.cleanroommc.invtweaks.sort;

import com.cleanroommc.invtweaks.InventoryTweaks;
import com.cleanroommc.invtweaks.api.ISortableContainer;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Comparator;

public class SortHandler {

    private final Container container;
    private final ISortableContainer sortableContainer;
    private GuiSortingContext context;

    public SortHandler(Container container, ISortableContainer sortableContainer) {
        this.container = container;
        this.sortableContainer = sortableContainer;
        GuiSortingContext.Builder builder = new GuiSortingContext.Builder(container);
        sortableContainer.buildSortingContext(builder);
        this.context = builder.build();
    }

    public void sort(int slotId) {
        InventoryTweaks.LOGGER.info("Sorting for slot {}", slotId);
        Slot[][] slotGroup = context.getSlotGroup(slotId);
        if (slotGroup != null) {
            sort(slotGroup);
        }
    }

    public void sort(Slot[][] slotGroup) {
        SortedMultiset<ItemStack> items = gatherItems(slotGroup);
        Multiset.Entry<ItemStack> entry = items.pollFirstEntry();
        ItemStack item = entry.getElement();
        int remaining = entry.getCount();
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
                    if (items.isEmpty()) {
                        item = ItemStack.EMPTY;
                        continue;
                    }
                    entry = items.pollFirstEntry();
                    item = entry.getElement();
                    remaining = entry.getCount();
                }
            }
        }
    }

    public SortedMultiset<ItemStack> gatherItems(Slot[][] slotGroup) {
        SortedMultiset<ItemStack> items = TreeMultiset.create(ITEM_COMPARATOR);
        for (Slot[] slotRow : slotGroup) {
            for (Slot slot : slotRow) {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    int amount = stack.getCount();
                    stack = stack.copy();
                    stack.setCount(1);
                    items.add(stack, amount);
                }
            }
        }
        return items;
    }

    public static final Comparator<ItemStack> ITEM_COMPARATOR = (stack1, stack2) -> {
        int result = InventoryTweaks.getMod(stack1).compareTo(InventoryTweaks.getMod(stack2));
        if (result != 0) return result;
        result = InventoryTweaks.getId(stack1).compareTo(InventoryTweaks.getId(stack2));
        if (result != 0) return result;
        result = Integer.compare(stack1.getMetadata(), stack2.getMetadata());
        if (result != 0) return result;
        if (stack1.hasTagCompound() || stack2.hasTagCompound()) {
            if (stack1.hasTagCompound() && !stack2.hasTagCompound()) return 1;
            if (!stack1.hasTagCompound()) return -1;
            if (stack1.getTagCompound().equals(stack2.getTagCompound())) return 0;
            return Integer.compare(stack1.getTagCompound().getSize(), stack2.getTagCompound().getSize());
        }
        return result;
    };
}
