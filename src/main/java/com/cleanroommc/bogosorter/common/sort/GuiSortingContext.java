package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GuiSortingContext {

    public static GuiSortingContext create(Container container) {
        GuiSortingContext.Builder builder = new GuiSortingContext.Builder(container);
        addPlayerInventory(builder, container);

        if (container instanceof ISortableContainer) {
            ((ISortableContainer) container).buildSortingContext(builder);
        } else {
            BogoSortAPI.INSTANCE.getBuilder(container).accept(container, builder);
        }
        return builder.build();
    }

    public static GuiSortingContext create(Container container, boolean player) {
        if (player) {
            GuiSortingContext.Builder builder = new GuiSortingContext.Builder(container);
            addPlayerInventory(builder, container);
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

    @Nullable
    public Slot[][] getNonPlayerSlotGroup() {
        for (Slot[][] slotGroup : slots) {
            if (slotGroup.length == 0 || slotGroup[0].length == 0) {
                continue;
            }
            if (!BogoSortAPI.isPlayerSlot(slotGroup[0][0])) {
                return slotGroup;
            }
        }
        return null;
    }

    @Nullable
    public Slot[][] getPlayerSlotGroup() {
        for (Slot[][] slotGroup : slots) {
            if (slotGroup.length == 0 || slotGroup[0].length == 0) {
                continue;
            }
            if (BogoSortAPI.isPlayerSlot(slotGroup[0][0])) {
                return slotGroup;
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

    private static void addPlayerInventory(GuiSortingContext.Builder builder, Container container) {
        List<Slot> slots = new ArrayList<>();
        for (Slot slot : container.inventorySlots) {
            if (slot.inventory instanceof InventoryPlayer ||
                    (slot instanceof SlotItemHandler &&
                            (((SlotItemHandler) slot).getItemHandler() instanceof PlayerMainInvWrapper || ((SlotItemHandler) slot).getItemHandler() instanceof PlayerInvWrapper))) {
                if (slot.getSlotIndex() >= 9 && slot.getSlotIndex() < 36) {
                    slots.add(slot);
                }
            }
        }
        if (!slots.isEmpty()) {
            builder.addSlotGroup(9, slots);
        }
    }
}
