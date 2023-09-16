package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISlotGroup;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;

public class SlotGroup implements ISlotGroup {

    private final boolean player;
    private final List<Slot> slots;
    private final int rowSize;
    private int priority;
    private IPosSetter posSetter;

    public SlotGroup(List<Slot> slots, int rowSize) {
        this(false, slots, rowSize);
    }

    public SlotGroup(boolean player, List<Slot> slots, int rowSize) {
        this.player = player;
        this.slots = Collections.unmodifiableList(slots);
        this.rowSize = rowSize;
        this.priority = 0;
        this.posSetter = IPosSetter.TOP_RIGHT_HORIZONTAL;
    }

    @Override
    public @UnmodifiableView List<Slot> getSlots() {
        return this.slots;
    }

    @Override
    public int getRowSize() {
        return this.rowSize;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public boolean isPlayerInventory() {
        return this.player;
    }

    @Nullable
    public IPosSetter getPosSetter() {
        return posSetter;
    }

    @Override
    public ISlotGroup priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public ISlotGroup buttonPosSetter(@Nullable IPosSetter posSetter) {
        this.posSetter = posSetter;
        return this;
    }

    public boolean hasSlot(int slotNumber) {
        for (Slot slot : getSlots()) {
            if (slot.slotNumber == slotNumber) return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return getSlots().isEmpty();
    }
}
