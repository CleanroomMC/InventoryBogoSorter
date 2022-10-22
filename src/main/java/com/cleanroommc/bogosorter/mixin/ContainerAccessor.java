package com.cleanroommc.bogosorter.mixin;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Container.class)
public interface ContainerAccessor {

    @Accessor
    List<IContainerListener> getListeners();
}
