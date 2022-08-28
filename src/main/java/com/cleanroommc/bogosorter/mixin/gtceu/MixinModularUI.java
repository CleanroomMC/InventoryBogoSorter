package com.cleanroommc.bogosorter.mixin.gtceu;

import com.cleanroommc.bogosorter.compat.gtce.IModularSortable;
import gregtech.api.gui.ModularUI;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ModularUI.class, remap = false)
public class MixinModularUI implements IModularSortable {

    @Unique
    @Final
    private Object2IntMap<String> rowSizes = new Object2IntOpenHashMap<>();

    @Override
    public void addSortArea(String key, int rowSize) {
        rowSizes.put(key, rowSize);
    }

    @Override
    public int getRowSize(String key) {
        return rowSizes.getInt(key);
    }
}
