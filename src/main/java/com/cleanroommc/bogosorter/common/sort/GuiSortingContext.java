package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.api.ISlotGroup;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.cleanroommc.bogosorter.common.lock.LockSlotCapability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.jetbrains.annotations.Nullable;
import ru.socol.expandableinventory.gui.ContainerExpandedInventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSortingContext {

    // Cache sorting context to avoid creating it multiple times while the container is open.
    // On multiplayer everyone accesses this, so it can be recreated more than once when multiple players access any gui at the
    // same time. However, this shouldn't be a problem since it's not multithreaded and the operation is usually not expensive.
    private static Container currentContainer;
    private static GuiSortingContext currentSortingContext;

    public static void invalidateCurrent() {
        currentSortingContext = null;
        currentContainer = null;
    }

    public static GuiSortingContext getOrCreate(Container container, EntityPlayer player) {
        if (currentContainer != container || currentSortingContext == null) {
            currentSortingContext = create(container, player);
            currentContainer = container;
        }
        return currentSortingContext;
    }

    public static GuiSortingContext create(Container container, EntityPlayer player) {
        GuiSortingContext.Builder builder = new GuiSortingContext.Builder(container);
        addPlayerInventory(builder, container, player);

        if (container instanceof ISortableContainer sc) {
            sc.buildSortingContext(builder);
        } else if (BogoSortAPI.isValidSortable(container)) {
            BogoSortAPI.INSTANCE.getBuilder(container).accept(container, builder);
        }
        return builder.build();
    }

    private final Container container;
    private final List<SlotGroup> slotGroups;
    private final int playerGroups;

    public GuiSortingContext(Container container, List<SlotGroup> slotGroups, int playerGroups) {
        slotGroups.sort(Comparator.comparingInt(SlotGroup::getPriority));
        this.container = container;
        this.slotGroups = slotGroups;
        this.playerGroups = playerGroups;
    }

    @Nullable
    public SlotGroup getSlotGroup(int id) {
        for (SlotGroup slotGroup : this.slotGroups) {
            if (slotGroup.hasSlot(id)) return slotGroup;
        }
        return null;
    }

    @Nullable
    public SlotGroup getNonPlayerSlotGroup() {
        for (SlotGroup slotGroup : this.slotGroups) {
            if (!isEmpty() && !slotGroup.isPlayerInventory()) return slotGroup;
        }
        return null;
    }

    @Nullable
    public SlotGroup getPlayerSlotGroup() {
        for (SlotGroup slotGroup : this.slotGroups) {
            if (slotGroup.isPlayerInventory() && !slotGroup.isHotbar()) return slotGroup;
        }
        return null;
    }

    public int getNonPlayerSlotGroupAmount() {
        return this.slotGroups.size() - this.playerGroups;
    }

    public Container getContainer() {
        return container;
    }

    public List<SlotGroup> getSlotGroups() {
        return slotGroups;
    }

    public boolean hasPlayer() {
        return this.playerGroups > 0;
    }

    public boolean isEmpty() {
        return this.slotGroups.isEmpty();
    }

    public static class Builder implements ISortingContextBuilder {

        private final Container container;
        private final List<SlotGroup> slots = new ArrayList<>();
        private int player = 0;

        public Builder(Container container) {
            this.container = container;
        }

        @Override
        public ISlotGroup addSlotGroupOf(List<Slot> slots, int rowSize) {
            return addSlotGroup(slots.stream().map(BogoSortAPI.INSTANCE::getSlot).collect(Collectors.toList()), rowSize);
        }

        @Override
        public ISlotGroup addSlotGroup(List<ISlot> slots, int rowSize) {
            if (slots.size() < rowSize) {
                throw new IllegalArgumentException("Slots must at least fill 1 row! Expected at least " + rowSize + " slot, but only found " + slots.size());
            }
            if (slots.size() < 2) {
                throw new IllegalArgumentException("Slot group must have at least 2 slots!");
            }
            SlotGroup slotGroup = new SlotGroup(slots, rowSize);
            this.slots.add(slotGroup);
            return slotGroup;
        }

        @Override
        public ISlotGroup addSlotGroup(int startIndex, int endIndex, int rowSize) {
            return addSlotGroupOf(this.container.inventorySlots.subList(startIndex, endIndex), rowSize);
        }

        @Override
        public ISlotGroup addGenericSlotGroup() {
            List<ISlot> slots = new ArrayList<>();
            IntArraySet xValues = new IntArraySet();
            for (Slot slot : this.container.inventorySlots) {
                ISlot iSlot = BogoSortAPI.INSTANCE.getSlot(slot);
                if (!BogoSortAPI.isPlayerMainInvSlot(iSlot)) {
                    slots.add(iSlot);
                    xValues.add(iSlot.bogo$getX());
                }
            }
            if (slots.size() < 2) {
                return SlotGroup.EMPTY;
            }
            return addSlotGroup(slots, xValues.size());
        }

        @Override
        public Container getContainer() {
            return this.container;
        }

        public GuiSortingContext build() {
            return new GuiSortingContext(container, slots, player);
        }
    }

    private static void addPlayerInventory(Builder builder, Container container, EntityPlayer player) {
        List<ISlot> slots = new ArrayList<>();
        List<ISlot> hotbar = new ArrayList<>();
        boolean all = BogoSorter.Mods.EXPANDABLE_INVENTORY.isLoaded() && container instanceof ContainerExpandedInventory;
        LockSlotCapability cap = LockSlotCapability.getForPlayer(player);
        for (Slot slot : container.inventorySlots) {
            if (BogoSortAPI.isPlayerMainInvSlot(slot)) {
                if (slot.getSlotIndex() < 9) hotbar.add(BogoSortAPI.INSTANCE.getSlot(slot));
                else slots.add(BogoSortAPI.INSTANCE.getSlot(slot));
            } else if (all) slots.add(BogoSortAPI.INSTANCE.getSlot(slot));
        }
        long mainInvMask = 0b000000000111111111111111111111111111L;
        long hotbarMask = 0b111111111;
        if (!slots.isEmpty()) {
            SlotGroup slotGroup = new SlotGroup(true, false, getUnlockedSlots(slots, cap, mainInvMask), slots, Math.min(9, slots.size()));
            slotGroup.priority(-10000)
                    .buttonPosSetter(BogoSortAPI.INSTANCE.getPlayerButtonPos(container));
            builder.slots.add(slotGroup);
            builder.player++;
        }
        if (!hotbar.isEmpty()) {
            SlotGroup slotGroup = new SlotGroup(true, true, getUnlockedSlots(hotbar, cap, hotbarMask), hotbar, Math.min(9, hotbar.size()));
            slotGroup.priority(-10000)
                    .buttonPosSetter(null);
            builder.slots.add(slotGroup);
            builder.player++;
        }
    }

    private static List<ISlot> getUnlockedSlots(List<ISlot> slots, LockSlotCapability cap, long mask) {
        if ((cap.getLockedSlots() & mask) == 0) {
            return slots;
        }
        List<ISlot> unlockedSlots = new ArrayList<>();
        for (ISlot slot : slots) {
            if (!cap.isSlotLocked(slot.bogo$getSlotIndex())) {
                unlockedSlots.add(slot);
            }
        }
        return unlockedSlots;
    }
}
