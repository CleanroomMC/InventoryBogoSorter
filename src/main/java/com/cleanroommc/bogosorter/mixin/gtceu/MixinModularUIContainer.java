package com.cleanroommc.bogosorter.mixin.gtceu;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.cleanroommc.bogosorter.compat.gtce.IModularSortable;
import com.cleanroommc.bogosorter.compat.gtce.SortableSlotWidget;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.ModularUIContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ModularUIContainer.class)
public class MixinModularUIContainer implements ISortableContainer {

    @Shadow
    @Final
    private ModularUI modularUI;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        Map<String, List<Slot>> sortableSlots = new HashMap<>();
        for (Widget widget : modularUI.getFlatVisibleWidgetCollection()) {
            if (widget instanceof SortableSlotWidget) {
                SortableSlotWidget sortableSlotWidget = (SortableSlotWidget) widget;
                if (sortableSlotWidget.getSortArea() != null) {
                    sortableSlots.computeIfAbsent(sortableSlotWidget.getSortArea(), key -> new ArrayList<>()).add(sortableSlotWidget.getHandle());
                }
            }
        }
        for (Map.Entry<String, List<Slot>> entry : sortableSlots.entrySet()) {
            int rowSize = getSortableModularUI().getRowSize(entry.getKey());
            if (rowSize > 0) {
                builder.addSlotGroup(rowSize, entry.getValue());
            }
        }
    }

    public IModularSortable getSortableModularUI() {
        return (IModularSortable) (Object) modularUI;
    }
}
