package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.api.ISlotGroup;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class SlotGroup implements ISlotGroup {

    private final List<Slot> slots;
    private final int rowSize;
    private final int priority;
    private final Consumer<Point> positionUpdater;

    public SlotGroup(List<Slot> slots, int rowSize, int priority, @Nullable Consumer<Point> positionUpdater) {
        this.slots = slots;
        this.rowSize = rowSize;
        this.priority = priority;
        this.positionUpdater = positionUpdater != null ? positionUpdater : point -> {
            if (getSlots().size() < rowSize) {
                point.setLocation(-1000, -1000);
            } else {
                Slot topRight = getSlots().get(this.rowSize - 1);
                point.setLocation(topRight.xPos + 18, topRight.yPos);
            }
        };
    }

    @Override
    public @UnmodifiableView List<Slot> getSlots() {
        return Collections.unmodifiableList(this.slots);
    }

    @Override
    public int getRowSize() {
        return this.rowSize;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public void updateTopRightPos(Point point) {
        this.positionUpdater.accept(point);
    }
}
