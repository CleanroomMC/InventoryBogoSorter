package com.cleanroommc.bogosorter.mixin.enderio;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import crazypants.enderio.base.item.darksteel.upgrade.storage.StorageContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(StorageContainer.class)
public class MixinEIOStorage implements ISortableContainer {

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        List<Slot> slots = new ArrayList<>();
        Set<Integer> slotX = new HashSet<>();
        for (Slot slot : ((Container) (Object) this).inventorySlots) {
            if (slot.isEnabled() && !(slot.inventory instanceof InventoryPlayer)) {
                slots.add(slot);
                slotX.add(slot.xPos);
            }
        }
        builder.addSlotGroup(slotX.size(), slots);
    }
}
