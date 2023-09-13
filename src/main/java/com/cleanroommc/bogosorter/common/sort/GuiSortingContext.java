package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
            if (slotGroup.isPlayerInventory()) return slotGroup;
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

        public Builder addSlotGroup(SlotGroup slotGroup) {
            this.slots.add(slotGroup);
            return this;
        }

        public ISortingContextBuilder addSlotGroup(int rowSize, int startIndex, int endIndex) {
            return addSlotGroup(rowSize, startIndex, endIndex, 0, null);
        }

        @Override
        public ISortingContextBuilder addSlotGroup(int rowSize, int startIndex, int endIndex, int priority) {
            return addSlotGroup(rowSize, startIndex, endIndex, priority, null);
        }

        @Override
        public ISortingContextBuilder addSlotGroup(int rowSize, int startIndex, int endIndex, int priority, BiConsumer<SlotGroup, Point> pointSetter) {
            if (endIndex - startIndex < rowSize) {
                throw new IllegalArgumentException("The start and end index must be at least apart by the row size!");
            }
            return addSlotGroup(rowSize, container.inventorySlots.subList(startIndex, endIndex), priority, pointSetter);
        }

        public ISortingContextBuilder addSlotGroup(int rowSize, List<Slot> slots) {
            return addSlotGroup(rowSize, slots, 0, null);
        }

        @Override
        public ISortingContextBuilder addSlotGroup(int rowSize, List<Slot> slots, int priority) {
            return addSlotGroup(rowSize, slots, priority, null);
        }

        @Override
        public ISortingContextBuilder addSlotGroup(int rowSize, List<Slot> slots, int priority, BiConsumer<SlotGroup, Point> posSetter) {
            if (slots.size() < rowSize) {
                throw new IllegalArgumentException("Slots needs fill at least 1 row! Found " + slots.size() + " slot, but expected at least " + rowSize);
            }
            return addSlotGroup(new SlotGroup(slots, rowSize, priority, posSetter));
        }

        public GuiSortingContext build() {
            return new GuiSortingContext(container, slots, player);
        }
    }

    private static void addPlayerInventory(GuiSortingContext.Builder builder, Container container) {
        List<Slot> slots = new ArrayList<>();
        for (Slot slot : container.inventorySlots) {
            if (BogoSortAPI.isPlayerSlot(slot)) slots.add(slot);
        }
        if (!slots.isEmpty()) {
            builder.addSlotGroup(9, slots);
            builder.player = true;
        }
    }
}
