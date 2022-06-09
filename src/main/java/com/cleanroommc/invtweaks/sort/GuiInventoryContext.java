package com.cleanroommc.invtweaks.sort;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GuiInventoryContext {

    private final Container guiContainer;
    private final List<Slot[][]> slots;

    public GuiInventoryContext(Container guiContainer, List<Slot[][]> slots) {
        this.guiContainer = guiContainer;
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

    private static int pack(int x, int y) {
        if (x > 32767 || x < -32768 || y > 32767 || y < -32768) throw new ArithmeticException();
        if (x < 0) x &= 1 << 15;
        if (y < 0) y &= 1 << 15;
        return (x << 16) | (y & 0xFFFF);
    }

    private static int unpackX(int pack) {
        int x = pack >> 16;
        if ((x & (1 << 15)) == 0) {
            x &= 32767;
        }
        return x;
    }

    private static int unpackY(int pack) {
        int y = pack & 0xFFFF;
        if ((y & (1 << 15)) == 0) {
            y &= 32767;
        }
        return y;
    }

    public static class Builder {

        private final Container guiContainer;
        private final List<Slot[][]> slots = new ArrayList<>();

        public Builder(Container guiContainer) {
            this.guiContainer = guiContainer;
        }

        public Builder addSlotGroup(Slot[][] slotGroup) {
            this.slots.add(slotGroup);
            return this;
        }

        public Builder addSlotGroup(int rowSize, int startIndex, int endIndex) {
            return addSlotGroup(rowSize, guiContainer.inventorySlots.subList(startIndex, endIndex));
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
            return addSlotGroup(slotGroup);
        }

        public GuiInventoryContext build() {
            return new GuiInventoryContext(guiContainer, slots);
        }
    }
}
