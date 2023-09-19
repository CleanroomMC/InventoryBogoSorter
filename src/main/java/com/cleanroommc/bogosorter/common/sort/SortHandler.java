package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.McUtils;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.network.CSlotSync;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
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

    public static SoundEvent sortSound = SoundEvents.UI_BUTTON_CLICK;

    @SideOnly(Side.CLIENT)
    public static void playSortSound() {
        SoundEvent sound = BogoSorter.isAprilFools() ? SoundEvent.REGISTRY.getObjectById(BogoSorter.RND.nextInt(SoundEvent.REGISTRY.getKeys().size())) : sortSound;
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(sound, 1f));
    }

    private static float getBogoChance() {
        return BogoSorter.isAprilFools() ? 0.2f : 0.01f;
    }

    private final EntityPlayer player;
    private final Container container;
    private final GuiSortingContext context;
    private final Comparator<ItemSortContainer> containerComparator;
    private final Int2ObjectMap<ClientSortData> clientSortData;
    private final List<SortRule<ItemStack>> itemSortRules;

    public SortHandler(EntityPlayer entityPlayer, Container container, Int2ObjectMap<ClientSortData> clientSortData) {
        this(entityPlayer, container, GuiSortingContext.getOrCreate(container), clientSortData);
    }

    public SortHandler(EntityPlayer player, Container container, GuiSortingContext sortingContext, Int2ObjectMap<ClientSortData> clientSortData) {
        this.player = player;
        this.container = container;
        this.context = sortingContext;
        this.itemSortRules = cacheItemSortRules.getOrDefault(player, Collections.emptyList());
        this.containerComparator = (container1, container2) -> {
            int result;
            for (SortRule<ItemStack> sortRule : itemSortRules) {
                result = sortRule instanceof ClientItemSortRule ? ((ClientItemSortRule) sortRule).compareServer(container1, container2) :
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
        SlotGroup slotGroup = context.getSlotGroup(slotId);
        sort(slotGroup, sync);
    }

    public void sort(SlotGroup slotGroup, boolean sync) {
        if (slotGroup != null) {
            if (BogoSorter.RND.nextFloat() < getBogoChance()) {
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

    public void sortHorizontal(SlotGroup slotGroup) {
        LinkedList<ItemSortContainer> itemList = gatherItems(slotGroup);
        if (itemList.isEmpty()) return;

        currentNbtSortRules.set(cacheNbtSortRules.getOrDefault(player, Collections.emptyList()));
        itemList.sort(containerComparator);
        currentNbtSortRules.set(Collections.emptyList());

        ItemSortContainer itemSortContainer = itemList.pollFirst();
        if (itemSortContainer == null) return;
        for (ISlot slot : slotGroup.getSlots()) {
            if (itemSortContainer == null) {
                slot.putStack(ItemStack.EMPTY);
                continue;
            }

            int max = Math.min(slot.getItemStackLimit(itemSortContainer.getItemStack()), slot.getMaxStackSize(itemSortContainer.getItemStack()));
            if (max <= 0) continue;
            slot.putStack(itemSortContainer.makeStack(max));

            if (!itemSortContainer.canMakeStack()) {
                itemSortContainer = itemList.pollFirst();
            }
        }
        if (!itemList.isEmpty()) {
            McUtils.giveItemsToPlayer(this.player, prepareDropList(itemList));
        }
    }

    // TODO untested
    /*public void sortVertical(SlotGroup slotGroup) {
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
    }*/

    public static void sortBogo(SlotGroup slotGroup) {
        List<ItemStack> items = new ArrayList<>();
        for (ISlot slot : slotGroup.getSlots()) {
            ItemStack stack = slot.getStack();
            items.add(stack);
        }
        Collections.shuffle(items);
        List<ISlot> slots = slotGroup.getSlots();
        for (int i = 0; i < slots.size(); i++) {
            ISlot slot = slots.get(i);
            slot.putStack(items.get(i));
        }
    }

    public LinkedList<ItemSortContainer> gatherItems(SlotGroup slotGroup) {
        LinkedList<ItemSortContainer> list = new LinkedList<>();
        Object2ObjectOpenCustomHashMap<ItemStack, ItemSortContainer> items = new Object2ObjectOpenCustomHashMap<>(BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);
        for (ISlot slot : slotGroup.getSlots()) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                ItemSortContainer container1 = items.get(stack);
                if (container1 == null) {
                    container1 = new ItemSortContainer(stack, clientSortData.get(slot.getSlotNumber()));
                    items.put(stack, container1);
                    list.add(container1);
                }
                container1.grow(stack.getCount());
            }
        }
        return list;
    }

    private static List<ItemStack> prepareDropList(List<ItemSortContainer> sortedList) {
        List<ItemStack> dropList = new ArrayList<>();
        for (ItemSortContainer itemSortContainer : sortedList) {
            while (itemSortContainer.canMakeStack()) {
                dropList.add(itemSortContainer.makeStack(itemSortContainer.getItemStack().getMaxStackSize()));
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

    public void clearAllItems(ISlot slot1) {
        SlotGroup slotGroup = context.getSlotGroup(slot1.getSlotNumber());
        if (slotGroup != null) {
            List<Pair<ItemStack, Integer>> slots = new ArrayList<>();
            for (ISlot slot : slotGroup.getSlots()) {
                if (!slot.getStack().isEmpty()) {
                    slot.putStack(ItemStack.EMPTY);
                    slots.add(Pair.of(ItemStack.EMPTY, slot.getSlotNumber()));
                }
            }
            NetworkHandler.sendToServer(new CSlotSync(slots));
        }
    }

    public void randomizeItems(ISlot slot1) {
        SlotGroup slotGroup = context.getSlotGroup(slot1.getSlotNumber());
        if (slotGroup != null) {
            List<Pair<ItemStack, Integer>> slots = new ArrayList<>();
            Random random = new Random();
            for (ISlot slot : slotGroup.getSlots()) {
                if (random.nextFloat() < 0.3f) {
                    ItemStack randomItem = ClientEventHandler.allItems.get(random.nextInt(ClientEventHandler.allItems.size())).copy();
                    slot.putStack(randomItem.copy());
                    slots.add(Pair.of(randomItem, slot.getSlotNumber()));
                }
            }
            NetworkHandler.sendToServer(new CSlotSync(slots));
        }
    }
}
