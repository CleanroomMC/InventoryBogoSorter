package com.cleanroommc.bogosorter.mixin.gtce;

import com.cleanroommc.bogosorter.compat.gtce.IModularSortable;
import gregtech.api.gui.ModularUI;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(ModularUI.class)
public class MixinModularUI implements IModularSortable {

    @Unique
    @Final
    private final Map<String, Integer> sortAreas = new HashMap<>();

    @Override
    public void addSortArea(String key, int rowSize) {
        sortAreas.put(key, rowSize);
    }

    @Override
    public int getRowSize(String key) {
        return sortAreas.get(key);
    }
}
