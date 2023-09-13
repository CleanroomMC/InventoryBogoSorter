package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

        public Builder addSlotGroup(int rowSize, int startIndex, int endIndex) {
            if (endIndex - startIndex < rowSize) {
                throw new IllegalArgumentException("The start and end index must be at least apart by the row size!");
            }
            return addSlotGroup(rowSize, container.inventorySlots.subList(startIndex, endIndex));
        }

        public Builder addSlotGroup(int rowSize, List<Slot> slots) {
            if (slots.size() < rowSize) {
                throw new IllegalArgumentException("Slots needs fill at least 1 row! Found " + slots.size() + " slot, but expected at least " + rowSize);
            }
            /*// create new list just to be save
            slots = new ArrayList<>(slots);
            // sort slots so they have the correct order when put in grid
            slots.sort((slot1, slot2) -> {
                if (slot1.yPos == slot2.yPos) return Integer.compare(slot1.xPos, slot2.xPos);
                return Integer.compare(slot1.yPos, slot2.yPos);
            });
            // determine row amount
            int rows = slots.size() / rowSize;
            if (slots.size() % rowSize != 0) rows++;
            // put slots into 2d array
            Slot[][] slotGroup = new Slot[rows][rowSize];
            for (int i = 0; i < slots.size(); i++) {
                slotGroup[i / rowSize][i % rowSize] = slots.get(i);
            }
            if (rows > slots.size() / rowSize) {
                int nulls = 0;
                Slot[] lastRow = slotGroup[rows - 1];
                for (Slot slot : lastRow) {
                    if (slot == null) nulls++;
                }
                slotGroup[rows - 1] = Arrays.copyOf(lastRow, lastRow.length - nulls);
            }
            return addSlotGroup(slotGroup);*/
            return addSlotGroup(new SlotGroup(slots, rowSize, 0, null));
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
