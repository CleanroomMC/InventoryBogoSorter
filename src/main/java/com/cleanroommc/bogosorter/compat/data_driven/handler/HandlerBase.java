package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlotGroup;
import com.cleanroommc.bogosorter.compat.data_driven.BogoCompatHandler;
import com.google.gson.JsonObject;
import net.minecraft.inventory.Container;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author ZZZank
 */
public abstract class HandlerBase implements BogoCompatHandler {
    public final String targetName;
    private Consumer<ISlotGroup> additional;

    public static String readClass(JsonObject o) {
        return o.get("target").getAsString();
    }

    public HandlerBase(String className) {
        this.targetName = Objects.requireNonNull(className);
    }

    public static <T> @NotNull Class<? extends T> toClass(String className, Class<T> typeFilter) {
        Class<?> c;
        try {
            c = Class.forName(
                className,
                false,
                GeneralHandler.class.getClassLoader()
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (!typeFilter.isAssignableFrom(c)) {
            throw new IllegalArgumentException(String.format("loaded class is not a subclass of %s", typeFilter.getName()));
        }
        return (Class<? extends T>) c;
    }

    @Nullable
    public Class<? extends Container> toClass() {
        return toClass(this.targetName, Container.class);
    }

    @Override
    public abstract void handle(IBogoSortAPI api);

    public void setAdditional(Consumer<ISlotGroup> additional) {
        this.additional = additional;
    }

    public void additionalAction(ISlotGroup slotGroup) {
        if (additional != null) {
            additional.accept(slotGroup);
        }
    }
}
