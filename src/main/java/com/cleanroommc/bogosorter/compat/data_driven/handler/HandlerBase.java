package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.BogoCompatHandler;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.utils.DataDrivenReflection;
import com.google.gson.JsonObject;
import net.minecraft.inventory.Container;

import java.util.Objects;
import java.util.Optional;

/**
 * @author ZZZank
 */
public abstract class HandlerBase implements BogoCompatHandler {
    protected final Class<? extends Container> target;
    protected final Optional<BogoCondition> condition;

    public static Class<? extends Container> readClass(JsonObject o) {
        return DataDrivenReflection.toClass(o.get("target"), Container.class);
    }

    protected HandlerBase(Optional<BogoCondition> condition, Class<? extends Container> target) {
        this.target = Objects.requireNonNull(target);
        this.condition = Objects.requireNonNull(condition);
    }

    @Override
    public void handle(IBogoSortAPI api) {
        if (condition.isEmpty() || condition.get().test()) {
            handleImpl(api);
        }
    }

    protected abstract void handleImpl(IBogoSortAPI api);

    public final Class<? extends Container> target() {
        return target;
    }

    public final Optional<BogoCondition> condition() {
        return condition;
    }
}
