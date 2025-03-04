package com.cleanroommc.bogosorter.core.mixin.enderio;

import crazypants.enderio.base.item.darksteel.upgrade.storage.StorageUpgrade;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = StorageUpgrade.class, remap = false)
public interface StorageUpgradeAccessor {

    @Invoker
    static int invokeCols(EntityEquipmentSlot slot) {
        return 0;
    }
}
