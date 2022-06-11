package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.SortRule;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class SortHandler {

    private static final List<SortRule<ItemStack>> sortRules = new ArrayList<>();
    private static final List<NbtSortRule> nbtSortRules = new ArrayList<>();

    static {
        String[] itemRules = {"mod", "material", "ore_prefix", "id", "meta", "nbt_has", "nbt_rules"};
        String[] nbtRules = {"enchantment", "enchantment_book", "potion", "gt_circ_config"};

        sortRules.addAll(Arrays.stream(itemRules).map(BogoSortAPI.INSTANCE::getItemSortRule).collect(Collectors.toList()));
        nbtSortRules.addAll(Arrays.stream(nbtRules).map(BogoSortAPI.INSTANCE::getNbtSortRule).collect(Collectors.toList()));
    }

    public static void updateSortRules(Collection<SortRule<ItemStack>> rules) {
        sortRules.clear();
        sortRules.addAll(rules);
    }

    public static void updateSortRules(SortRule<ItemStack>... rules) {
        updateSortRules(Arrays.asList(rules));
    }

    private final Container container;
    private GuiSortingContext context;

    public SortHandler(Container container, boolean player) {
        this.container = container;
        this.context = createSortContext(player);
    }

    public GuiSortingContext createSortContext(boolean player) {
        if (player) {
            GuiSortingContext.Builder builder = new GuiSortingContext.Builder(container);
            List<Slot> slots = new ArrayList<>();
            for (Slot slot : container.inventorySlots) {
                if (slot.inventory instanceof InventoryPlayer && slot.getSlotIndex() >= 9 && slot.getSlotIndex() < 36) {
                    slots.add(slot);
                }
            }
            builder.addSlotGroup(9, slots);
            return builder.build();
        }
        if (container instanceof ISortableContainer) {
            GuiSortingContext.Builder builder = new GuiSortingContext.Builder(container);
            ((ISortableContainer) container).buildSortingContext(builder);
            return builder.build();
        }
        if (BogoSortAPI.isValidSortable(container)) {
            GuiSortingContext.Builder builder = new GuiSortingContext.Builder(container);
            BogoSortAPI.INSTANCE.getBuilder(container).accept(container, builder);
            return builder.build();
        }
        return new GuiSortingContext(container, Collections.emptyList());
    }

    public void sort(int slotId) {
        Slot[][] slotGroup = context.getSlotGroup(slotId);
        if (slotGroup != null) {
            sort(slotGroup);
        }
    }

    public void sort(Slot[][] slotGroup) {
        Object2IntMap<ItemStack> items = gatherItems(slotGroup);
        if (items.isEmpty()) return;
        LinkedList<ItemStack> itemList = new LinkedList<>(items.keySet());
        itemList.forEach(item -> item.setCount(items.getInt(item)));
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
        Object2IntOpenCustomHashMap<ItemStack> items = new Object2IntOpenCustomHashMap<>(BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);
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
        for (SortRule<ItemStack> sortRule : sortRules) {
            result = sortRule.compare(stack1, stack2);
            if (result != 0) return result;
        }
        return result;
    };

    public static List<NbtSortRule> getNbtSortRules() {
        return nbtSortRules;
    }
}
