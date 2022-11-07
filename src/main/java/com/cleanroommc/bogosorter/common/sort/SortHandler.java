package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.McUtils;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.network.CSlotSync;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SortHandler {

    public static final Map<EntityPlayer, List<SortRule<ItemStack>>> cacheItemSortRules = new Object2ObjectOpenHashMap<>();
    public static final Map<EntityPlayer, List<NbtSortRule>> cacheNbtSortRules = new Object2ObjectOpenHashMap<>();
    public static final AtomicReference<List<NbtSortRule>> currentNbtSortRules = new AtomicReference<>(Collections.emptyList());

    private final EntityPlayer player;
    private final Container container;
    private final GuiSortingContext context;
    private final Comparator<ItemSortContainer> containerComparator;
    private final Int2ObjectMap<ClientSortData> clientSortData;
    private final List<SortRule<ItemStack>> itemSortRules;

    public SortHandler(EntityPlayer entityPlayer, Container container, boolean player, Int2ObjectMap<ClientSortData> clientSortData) {
        this(entityPlayer, container, GuiSortingContext.create(container, player), clientSortData);
    }

    public SortHandler(EntityPlayer player, Container container, GuiSortingContext sortingContext, Int2ObjectMap<ClientSortData> clientSortData) {
        this.player = player;
        this.container = container;
        this.context = sortingContext;
        this.itemSortRules = cacheItemSortRules.getOrDefault(player, Collections.emptyList());
        this.containerComparator = (container1, container2) -> {
            int result;
            for (SortRule<ItemStack> sortRule : itemSortRules) {
                result = sortRule instanceof ClientItemSortRule ? ((ClientItemSortRule) sortRule).compareClient(container1, container2) :
                        sortRule.compare(container1.getItemStack(), container2.getItemStack());
                if (result != 0) return result;
            }
            result = ItemCompareHelper.compareRegistryOrder(container1.getItemStack(), container2.getItemStack());
            if (result != 0) return result;
            result = ItemCompareHelper.compareMeta(container1.getItemStack(), container2.getItemStack());
            return result;
        };
        this.clientSortData = clientSortData;
    }

    public void sort(int slotId) {
        sort(slotId, true);
    }

    public void sort(int slotId, boolean sync) {
        Slot[][] slotGroup = context.getSlotGroup(slotId);
        sort(slotGroup, sync);
    }

    public void sort(Slot[][] slotGroup, boolean sync) {
        if (slotGroup != null) {
            if (new Random().nextFloat() < 0.0005f) {
                sortBogo(slotGroup);
                this.player.sendMessage(new TextComponentString("Get Bogo'd!"));
            } else {
                sortHorizontal(slotGroup);
            }
            if (sync) {
                container.detectAndSendChanges();
            }
        }
    }

    public void sortHorizontal(Slot[][] slotGroup) {
        LinkedList<ItemSortContainer> itemList = gatherItems(slotGroup);
        if (itemList.isEmpty()) return;

        currentNbtSortRules.set(cacheNbtSortRules.getOrDefault(player, Collections.emptyList()));
        itemList.sort(containerComparator);
        currentNbtSortRules.set(Collections.emptyList());

        ItemSortContainer itemSortContainer = itemList.pollFirst();
        if (itemSortContainer == null) return;
        for (Slot[] slotRow : slotGroup) {
            for (Slot slot : slotRow) {
                if (itemSortContainer == null) {
                    slot.putStack(ItemStack.EMPTY);
                    continue;
                }

                int max = slot.getItemStackLimit(itemSortContainer.getItemStack());
                if (max <= 0) continue;
                slot.putStack(itemSortContainer.makeStack(max));

                if (!itemSortContainer.canMakeStack()) {
                    itemSortContainer = itemList.pollFirst();
                }
            }
        }
        if (!itemList.isEmpty()) {
            McUtils.giveItemsToPlayer(this.player, prepareDropList(itemList));
        }
    }

    // TODO untested
    public void sortVertical(Slot[][] slotGroup) {
        LinkedList<ItemSortContainer> itemList = gatherItems(slotGroup);
        if (itemList.isEmpty()) return;

        currentNbtSortRules.set(cacheNbtSortRules.getOrDefault(player, Collections.emptyList()));
        itemList.sort(containerComparator);
        currentNbtSortRules.set(Collections.emptyList());

        ItemSortContainer itemSortContainer = itemList.pollFirst();
        if (itemSortContainer == null) return;
        main:
        for (int c = 0; c < slotGroup[0].length; c++) {
            for (Slot[] slots : slotGroup) {
                if (c >= slots.length) break main;
                Slot slot = slots[c];
                if (itemSortContainer == null) {
                    slot.putStack(ItemStack.EMPTY);
                    continue;
                }
                if (!itemSortContainer.canMakeStack()) {
                    itemSortContainer = itemList.pollFirst();
                    if (itemSortContainer == null) continue;
                }
                int max = slot.getItemStackLimit(itemSortContainer.getItemStack());
                if (max <= 0) continue;
                slot.putStack(itemSortContainer.makeStack(max));
            }
        }
        if (!itemList.isEmpty()) {
            McUtils.giveItemsToPlayer(this.player, prepareDropList(itemList));
        }
    }

    public static void sortBogo(Slot[][] slotGroup) {
        ItemStack[][] itemGrid = new ItemStack[slotGroup.length][slotGroup[0].length];
        for (ItemStack[] itemRow : itemGrid) {
            Arrays.fill(itemRow, ItemStack.EMPTY);
        }
        List<ItemStack> items = new ArrayList<>();
        for (Slot[] slotRow : slotGroup) {
            for (Slot slot : slotRow) {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }
        Random rnd = new Random();
        for (ItemStack item : items) {
            int slot, row;
            do {
                row = rnd.nextInt(itemGrid.length);
                slot = rnd.nextInt(itemGrid[row].length);
            } while (!itemGrid[row][slot].isEmpty());
            itemGrid[row][slot] = item;
        }
        for (int r = 0; r < slotGroup.length; r++) {
            for (int c = 0; c < slotGroup[r].length; c++) {
                slotGroup[r][c].putStack(itemGrid[r][c]);
            }
        }
    }

    public LinkedList<ItemSortContainer> gatherItems(Slot[][] slotGroup) {
        LinkedList<ItemSortContainer> list = new LinkedList<>();
        Object2ObjectOpenCustomHashMap<ItemStack, ItemSortContainer> items = new Object2ObjectOpenCustomHashMap<>(BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);
        for (Slot[] slotRow : slotGroup) {
            for (Slot slot : slotRow) {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    ItemSortContainer container1 = items.get(stack);
                    if (container1 == null) {
                        container1 = new ItemSortContainer(stack, clientSortData.get(slot.slotNumber));
                        items.put(stack, container1);
                        list.add(container1);
                    }
                    container1.grow(stack.getCount());
                }
            }
        }
        return list;
    }

    private static List<ItemStack> prepareDropList(List<ItemSortContainer> sortedList) {
        List<ItemStack> dropList = new ArrayList<>();
        for (ItemSortContainer itemSortContainer : sortedList) {
            while (itemSortContainer.canMakeStack()) {
                dropList.add(itemSortContainer.makeStack(Integer.MAX_VALUE));
            }
        }
        return dropList;
    }

    @SideOnly(Side.CLIENT)
    public static Comparator<ItemStack> getClientItemComparator() {
        return (stack1, stack2) -> {
            int result = 0;
            for (SortRule<ItemStack> sortRule : BogoSorterConfig.sortRules) {
                result = sortRule.compare(stack1, stack2);
                if (result != 0) return result;
            }
            result = ItemCompareHelper.compareRegistryOrder(stack1, stack2);
            if (result != 0) return result;
            result = ItemCompareHelper.compareMeta(stack1, stack2);
            return result;
        };
    }

    public void clearAllItems(Slot slot1) {
        Slot[][] slotGroup = context.getSlotGroup(slot1.slotNumber);
        if (slotGroup != null) {
            List<Pair<ItemStack, Integer>> slots = new ArrayList<>();
            for (Slot[] slotRow : slotGroup) {
                for (Slot slot : slotRow) {
                    if (!slot.getStack().isEmpty()) {
                        slot.putStack(ItemStack.EMPTY);
                        slots.add(Pair.of(ItemStack.EMPTY, slot.slotNumber));
                    }
                }
            }
            NetworkHandler.sendToServer(new CSlotSync(slots));
        }
    }

    public void randomizeItems(Slot slot1) {
        Slot[][] slotGroup = context.getSlotGroup(slot1.slotNumber);
        if (slotGroup != null) {
            List<Pair<ItemStack, Integer>> slots = new ArrayList<>();
            Random random = new Random();
            for (Slot[] slotRow : slotGroup) {
                for (Slot slot : slotRow) {
                    if (random.nextFloat() < 0.3f) {
                        ItemStack randomItem = ClientEventHandler.allItems.get(random.nextInt(ClientEventHandler.allItems.size())).copy();
                        slot.putStack(randomItem.copy());
                        slots.add(Pair.of(randomItem, slot.slotNumber));
                    }
                }
            }
            NetworkHandler.sendToServer(new CSlotSync(slots));
        }
    }
}
