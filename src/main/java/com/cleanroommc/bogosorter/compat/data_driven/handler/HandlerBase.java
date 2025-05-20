package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.BogoCompatHandler;
import com.cleanroommc.bogosorter.compat.data_driven.utils.DataDrivenReflection;
import com.google.gson.JsonObject;
import net.minecraft.inventory.Container;

import java.util.Objects;

/**
 * @author ZZZank
 */
public abstract class HandlerBase implements BogoCompatHandler {
    public final Class<? extends Container> target;

    public static Class<? extends Container> readClass(JsonObject o) {
        return DataDrivenReflection.toClass(o.get("target"), Container.class);
    }

    protected HandlerBase(Class<? extends Container> target) {
        this.target = Objects.requireNonNull(target);
    }

    @Override
    public abstract void handle(IBogoSortAPI api);
}
