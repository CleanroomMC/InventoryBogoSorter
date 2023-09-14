package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.api.*;
import com.cleanroommc.bogosorter.common.sort.ClientItemSortRule;
import com.cleanroommc.bogosorter.common.sort.ItemSortContainer;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import com.cleanroommc.bogosorter.core.mixin.ItemStackAccessor;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BogoSortAPI implements IBogoSortAPI {

    public static final BogoSortAPI INSTANCE = new BogoSortAPI();
    public static final SortRule<ItemStack> EMPTY_ITEM_SORT_RULE = new SortRule<ItemStack>("empty", null, (o1, o2) -> 0) {
        @Override
        public boolean isEmpty() {
            return true;
        }
    };
    public static final NbtSortRule EMPTY_NBT_SORT_RULE = new NbtSortRule("empty", null, (o1, o2) -> 0) {
        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    private BogoSortAPI() {
    }

    private final Map<Class<?>, BiConsumer<Container, ISortingContextBuilder>> COMPAT_MAP = new Object2ObjectOpenHashMap<>();
    private final Map<Class<?>, IPosSetter> playerButtonPos = new Object2ObjectOpenHashMap<>();
    private final Map<String, SortRule<ItemStack>> itemSortRules = new Object2ObjectOpenHashMap<>();
    private final Map<String, NbtSortRule> nbtSortRules = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<SortRule<ItemStack>> itemSortRules2 = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<NbtSortRule> nbtSortRules2 = new Int2ObjectOpenHashMap<>();
    // lists for fast iteration
    private final List<SortRule<ItemStack>> itemSortRuleList = new ArrayList<>();
    private final List<NbtSortRule> nbtSortRuleList = new ArrayList<>();
    // a map of all rules that changed through versions for json parsing
    private final Map<String, String> remappedSortRules = new Object2ObjectOpenHashMap<>();

    public void remapSortRule(String old, String newName) {
        this.remappedSortRules.put(old, newName);
    }

    @Override
    public <T extends Container> void addCompat(Class<T> clazz, BiConsumer<T, ISortingContextBuilder> builder) {
        COMPAT_MAP.put(clazz, (BiConsumer<Container, ISortingContextBuilder>) builder);
    }

    @Override
    public <T> void addCompatSimple(Class<T> clazz, BiConsumer<T, ISortingContextBuilder> builder) {
        if (!Container.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class must be an instance of Container!");
        }
        COMPAT_MAP.put(clazz, (BiConsumer<Container, ISortingContextBuilder>) builder);
    }

    @Override
    public <T extends Container> void addPlayerSortButtonPosition(Class<T> clazz, @Nullable IPosSetter buttonPos) {
        this.playerButtonPos.put(clazz, buttonPos);
    }

    @Override
    public <T extends Container> void removeCompat(Class<T> clazz) {
        COMPAT_MAP.remove(clazz);
    }

    private static void validateKey(String key) {
        if (!key.matches("[A-Za-z_]+")) {
            throw new IllegalArgumentException("Key must only have letters and underscores!");
        }
    }

    @Override
    public void registerItemSortingRule(String key, SortType type, Comparator<ItemStack> itemComparator) {
        validateKey(key);
        SortRule<ItemStack> sortRule = new SortRule<>(key, type, itemComparator);
        itemSortRules.put(key, sortRule);
        itemSortRuleList.add(sortRule);
        itemSortRules2.put(sortRule.getSyncId(), sortRule);
    }

    @ApiStatus.Internal
    @Override
    public void registerClientItemSortingRule(String key, SortType type, Comparator<ItemStack> comparator, Comparator<ItemSortContainer> serverComparator) {
        validateKey(key);
        ClientItemSortRule sortRule = new ClientItemSortRule(key, type, comparator, serverComparator);
        itemSortRules.put(key, sortRule);
        itemSortRuleList.add(sortRule);
        itemSortRules2.put(sortRule.getSyncId(), sortRule);
    }

    @Override
    public void registerNbtSortingRule(String key, String tagPath, Comparator<NBTBase> comparator) {
        validateKey(key);
        NbtSortRule sortRule = new NbtSortRule(key, tagPath, comparator);
        nbtSortRules.put(key, sortRule);
        nbtSortRuleList.add(sortRule);
        nbtSortRules2.put(sortRule.getSyncId(), sortRule);
    }

    @Override
    public void registerNbtSortingRule(String key, String tagPath, int expectedType) {
        validateKey(key);
        NbtSortRule sortRule = new NbtSortRule(key, tagPath, expectedType);
        nbtSortRules.put(key, sortRule);
        nbtSortRuleList.add(sortRule);
        nbtSortRules2.put(sortRule.getSyncId(), sortRule);
    }

    @Override
    public <T> void registerNbtSortingRule(String key, String tagPath, int expectedType, Comparator<T> comparator, Function<NBTBase, T> converter) {
        validateKey(key);
        NbtSortRule sortRule = new NbtSortRule(key, tagPath, expectedType, comparator, converter);
        nbtSortRules.put(key, sortRule);
        nbtSortRuleList.add(sortRule);
        nbtSortRules2.put(sortRule.getSyncId(), sortRule);
    }

    public <T extends Container> BiConsumer<T, ISortingContextBuilder> getBuilder(Container container) {
        BiConsumer<Container, ISortingContextBuilder> builder = COMPAT_MAP.get(container.getClass());
        return builder == null ? null : (BiConsumer<T, ISortingContextBuilder>) builder;
    }

    public <T extends Container> IPosSetter getPlayerButtonPos(Class<T> clazz) {
        return this.playerButtonPos.getOrDefault(clazz, SlotGroup.DEFAULT_POS_SETTER);
    }

    @Unmodifiable
    public List<NbtSortRule> getNbtSortRuleList() {
        return Collections.unmodifiableList(nbtSortRuleList);
    }

    @Unmodifiable
    public List<SortRule<ItemStack>> getItemSortRuleList() {
        return Collections.unmodifiableList(itemSortRuleList);
    }

    public SortRule<ItemStack> getItemSortRule(String key) {
        SortRule<ItemStack> sortRule = this.itemSortRules.get(key);
        if (sortRule == null && this.remappedSortRules.containsKey(key)) {
            sortRule = this.itemSortRules.get(this.remappedSortRules.get(key));
        }
        return sortRule == null ? EMPTY_ITEM_SORT_RULE : sortRule;
    }

    public SortRule<ItemStack> getItemSortRule(int syncId) {
        return itemSortRules2.get(syncId);
    }

    public NbtSortRule getNbtSortRule(int syncId) {
        return nbtSortRules2.get(syncId);
    }

    public NbtSortRule getNbtSortRule(String key) {
        NbtSortRule sortRule = this.nbtSortRules.get(key);
        if (sortRule == null && this.remappedSortRules.containsKey(key)) {
            sortRule = this.nbtSortRules.get(this.remappedSortRules.get(key));
        }
        return sortRule == null ? EMPTY_NBT_SORT_RULE : sortRule;
    }

    public static boolean isValidSortable(Container container) {
        return container instanceof ISortableContainer || INSTANCE.COMPAT_MAP.containsKey(container.getClass());
    }

    public static boolean isPlayerSlot(Slot slot) {
        if (slot == null) return false;
        if (slot.inventory instanceof InventoryPlayer) {
            return slot.getSlotIndex() >= 9 && slot.getSlotIndex() < 36;
        }
        if (slot instanceof SlotItemHandler) {
            IItemHandler iItemHandler = ((SlotItemHandler) slot).getItemHandler();
            if (iItemHandler instanceof PlayerMainInvWrapper || iItemHandler instanceof PlayerInvWrapper) {
                return slot.getSlotIndex() >= 9 && slot.getSlotIndex() < 36;
            }
        }
        return false;
    }

    public static boolean isPlayerOrHotbarSlot(Slot slot) {
        if (slot == null) return false;
        if (slot.inventory instanceof InventoryPlayer) {
            return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
        }
        if (slot instanceof SlotItemHandler) {
            IItemHandler iItemHandler = ((SlotItemHandler) slot).getItemHandler();
            if (iItemHandler instanceof PlayerMainInvWrapper || iItemHandler instanceof PlayerInvWrapper) {
                return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
            }
        }
        return false;
    }

    public static final Hash.Strategy<ItemStack> ITEM_META_NBT_HASH_STRATEGY = new Hash.Strategy<ItemStack>() {

        @Override
        public int hashCode(ItemStack o) {
            return Objects.hash(o.getItem(), o.getMetadata(), o.getTagCompound(), getItemAccessor(o).getCapNBT());
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == b) return true;
            if (a == null || b == null) return false;
            return (a.isEmpty() && b.isEmpty()) ||
                    (a.getItem() == b.getItem() &&
                            a.getMetadata() == b.getMetadata() &&
                            Objects.equals(a.getTagCompound(), b.getTagCompound()) &&
                            Objects.equals(getItemAccessor(a).getCapNBT(), getItemAccessor(b).getCapNBT()));
        }
    };

    public static final Hash.Strategy<ItemStack> ITEM_META_HASH_STRATEGY = new Hash.Strategy<ItemStack>() {

        @Override
        public int hashCode(ItemStack o) {
            return Objects.hash(o.getItem(), o.getMetadata());
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == b) return true;
            if (a == null || b == null) return false;
            return (a.isEmpty() && b.isEmpty()) ||
                    (a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata());
        }
    };

    public static ItemStackAccessor getItemAccessor(ItemStack itemStack) {
        return (ItemStackAccessor) (Object) itemStack;
    }
}
