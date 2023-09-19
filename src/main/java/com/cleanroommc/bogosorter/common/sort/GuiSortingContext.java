package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlotGroup;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GuiSortingContext {

    private static Container currentContainer;
    private static GuiSortingContext currentSortingContext;

    public static GuiSortingContext getOrCreate(Container container) {
        if (currentContainer != container) {
            currentSortingContext = create(container);
            currentContainer = container;
        }
        return currentSortingContext;
    }

    public static GuiSortingContext create(Container container) {
        GuiSortingContext.Builder builder = new GuiSortingContext.Builder(container);
        addPlayerInventory(builder, container);

        if (container instanceof ISortableContainer) {
            ((ISortableContainer) container).buildSortingContext(builder);
        } else if (BogoSortAPI.isValidSortable(container)) {
            BogoSortAPI.INSTANCE.getBuilder(container).accept(container, builder);
        }
        return builder.build();
    }

    private final Container container;
    private final List<SlotGroup> slotGroups;
    private final boolean hasPlayer;

    public GuiSortingContext(Container container, List<SlotGroup> slotGroups, boolean hasPlayer) {
        slotGroups.sort(Comparator.comparingInt(SlotGroup::getPriority));
        this.container = container;
        this.slotGroups = slotGroups;
        this.hasPlayer = hasPlayer;
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
        if (this.hasPlayer) {
            return this.slotGroups.size() - 1;
        }
        return this.slotGroups.size();
    }

    public Container getContainer() {
        return container;
    }

    public List<SlotGroup> getSlotGroups() {
        return slotGroups;
    }

    public boolean hasPlayer() {
        return hasPlayer;
    }

    public boolean isEmpty() {
        return this.slotGroups.isEmpty();
    }

    public static class Builder implements ISortingContextBuilder {

        private final Container container;
        private final List<SlotGroup> slots = new ArrayList<>();
        private boolean player = false;

        public Builder(Container container) {
            this.container = container;
        }

        @Override
        public ISlotGroup addSlotGroup(List<Slot> slots, int rowSize) {
            if (slots.size() < rowSize) {
                throw new IllegalArgumentException("Slots must at least fill 1 row! Expected at least " + rowSize + " slot, but only found " + slots.size());
            }
            SlotGroup slotGroup = new SlotGroup(slots, rowSize);
            this.slots.add(slotGroup);
            return slotGroup;
        }

        @Override
        public ISlotGroup addSlotGroup(int startIndex, int endIndex, int rowSize) {
            return addSlotGroup(this.container.inventorySlots.subList(startIndex, endIndex), rowSize);
        }

        @Override
        public Container getContainer() {
            return this.container;
        }

        public GuiSortingContext build() {
            return new GuiSortingContext(container, slots, player);
        }
    }

    private static void addPlayerInventory(GuiSortingContext.Builder builder, Container container) {
        List<Slot> slots = new ArrayList<>();
        List<Slot> hotbar = new ArrayList<>();
        for (Slot slot : container.inventorySlots) {
            if (BogoSortAPI.isPlayerSlot(slot)) {
                if (slot.getSlotIndex() < 9) hotbar.add(slot);
                else slots.add(slot);
            }
        }
        if (!slots.isEmpty()) {
            SlotGroup slotGroup = new SlotGroup(true, false, slots, Math.min(9, slots.size()));
            slotGroup.priority(-10000)
                    .buttonPosSetter(BogoSortAPI.INSTANCE.getPlayerButtonPos(container));
            builder.slots.add(slotGroup);
            builder.player = true;
        }
        if (!hotbar.isEmpty()) {
            SlotGroup slotGroup = new SlotGroup(true, true, hotbar, Math.min(9, hotbar.size()));
            slotGroup.priority(-10000)
                    .buttonPosSetter(null);
            builder.slots.add(slotGroup);
            builder.player = true;
        }
    }
}
