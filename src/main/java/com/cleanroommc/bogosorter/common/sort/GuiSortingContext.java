package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiSortingContext {

    private final Container container;
    private final List<Slot[][]> slots;

    public GuiSortingContext(Container container, List<Slot[][]> slots) {
        this.container = container;
        this.slots = slots;
    }

    @Nullable
    public Slot[][] getSlotGroup(int id) {
        for (Slot[][] slotGroup : slots) {
            for (Slot[] slotRow : slotGroup) {
                for (Slot slot : slotRow) {
                    if (id == slot.slotNumber) return slotGroup;
                }
            }
        }
        return null;
    }

    public Container getContainer() {
        return container;
    }

    public static class Builder implements ISortingContextBuilder {

        private final Container container;
        private final List<Slot[][]> slots = new ArrayList<>();

        public Builder(Container container) {
            this.container = container;
        }

        public Builder addSlotGroup(Slot[][] slotGroup) {
            this.slots.add(slotGroup);
            return this;
        }

        public Builder addSlotGroup(int rowSize, int startIndex, int endIndex) {
            return addSlotGroup(rowSize, container.inventorySlots.subList(startIndex, endIndex));
        }

        public Builder addSlotGroup(int rowSize, List<Slot> slots) {
            // create new list just to be save
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
            return addSlotGroup(slotGroup);
        }

        public GuiSortingContext build() {
            return new GuiSortingContext(container, slots);
        }
    }
}
