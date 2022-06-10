package com.cleanroommc.invtweaks.api;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class InventoryTweaksAPI {

    public static boolean dirtySlotLayout = true;
    private static final Map<Class<?>, BiConsumer<Container, ISortingContextBuilder>> COMPAT_MAP = new HashMap<>();

    public static void markGuiDirty() {
        dirtySlotLayout = true;
    }

    public static <T extends Container> void addCompat(Class<T> clazz, BiConsumer<T, ISortingContextBuilder> builder) {
        COMPAT_MAP.put(clazz, (BiConsumer<Container, ISortingContextBuilder>) builder);
    }

    public static <T extends Container> void removeCompat(Class<T> clazz) {
        COMPAT_MAP.remove(clazz);
    }

    public static <T extends Container> BiConsumer<T, ISortingContextBuilder> getBuilder(Container container) {
        BiConsumer<Container, ISortingContextBuilder> builder = COMPAT_MAP.get(container.getClass());
        return builder == null ? null : (BiConsumer<T, ISortingContextBuilder>) builder;
    }

    public static boolean isValidSortable(Container container) {
        return container instanceof ISortableContainer || COMPAT_MAP.containsKey(container.getClass());
    }

    public static boolean isPlayerSlot(Container container, int slot) {
        if (slot < 0) return false;
        Slot slot1 = container.getSlot(slot);
        return slot1.inventory instanceof InventoryPlayer && slot1.getSlotIndex() >= 9 && slot1.getSlotIndex() < 36;
    }
}
