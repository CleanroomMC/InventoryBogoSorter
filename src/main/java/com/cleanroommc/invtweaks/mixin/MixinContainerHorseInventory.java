package com.cleanroommc.invtweaks.mixin;

import com.cleanroommc.invtweaks.api.ISortableContainer;
import com.cleanroommc.invtweaks.api.ISortingContextBuilder;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.inventory.ContainerHorseInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerHorseInventory.class)
public class MixinContainerHorseInventory implements ISortableContainer {

    @Shadow @Final private AbstractHorse horse;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        if(horse instanceof AbstractChestHorse && ((AbstractChestHorse) horse).hasChest()) {
            builder.addSlotGroup(((AbstractChestHorse)horse).getInventoryColumns(), 2, 3 * ((AbstractChestHorse)horse).getInventoryColumns() + 2);
        }
    }
}
