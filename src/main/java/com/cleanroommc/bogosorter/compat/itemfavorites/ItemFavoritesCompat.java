package com.cleanroommc.bogosorter.compat.itemfavorites;

import com.cleanroommc.bogosorter.api.ISlot;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;

public class ItemFavoritesCompat {
    private static final boolean IS_LOADED = Loader.isModLoaded("itemfav");
    private static Method isSlotLockedMethod = null;
    private static Method isPlayerSlotLockedMethod = null;

    static {
        if (IS_LOADED) {
            try {
                Class<?> lockHandlerClass = Class.forName("mrunknown404.itemfav.utils.LockHandler");
                // Try to get the method with Slot parameter
                try {
                    isSlotLockedMethod = lockHandlerClass.getMethod("isSlotLocked", Slot.class);
                } catch (NoSuchMethodException e) {
                    // If no method with Slot parameter, try to get the method with int parameter
                    try {
                        isPlayerSlotLockedMethod = lockHandlerClass.getMethod("isSlotLocked", int.class);
                    } catch (NoSuchMethodException ignored) {
                    }
                }
            } catch (Exception e) {
                // If reflection fails, log the error but don't crash
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if a slot is locked by ItemFavorites
     * @param slot The slot to check
     * @return True if the slot is locked, false otherwise
     */
    public static boolean isSlotLocked(ISlot slot) {
        if (!IS_LOADED || (isSlotLockedMethod == null && isPlayerSlotLockedMethod == null)) {
            return false;
        }

        try {
            if (isSlotLockedMethod != null) {
                // If there's a method with Slot parameter, try to get the original Slot object
                Object originalSlot = getOriginalSlot(slot);
                if (originalSlot instanceof Slot) {
                    return (boolean) isSlotLockedMethod.invoke(null, originalSlot);
                }
            } else if (isPlayerSlotLockedMethod != null) {
                // If only method with int parameter exists, check if it's a player inventory slot
                int slotNumber = slot.bogo$getSlotNumber();
                // Player inventory slot range is usually 0-35 (36 slots), hotbar is 0-8
                if (slotNumber >= 0 && slotNumber < 36) {
                    return (boolean) isPlayerSlotLockedMethod.invoke(null, slotNumber);
                }
            }
        } catch (Exception e) {
            // If reflection call fails, return false
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Try to get the original Slot object from an ISlot
     * @param slot The ISlot instance
     * @return The original Slot object, or null if it can't be obtained
     */
    private static Object getOriginalSlot(ISlot slot) {
        try {
            // Check if ISlot is a wrapper around Slot
            if (slot instanceof Slot) {
                return slot;
            }
            
            // Try to get delegate or original slot
            Method getDelegateMethod = slot.getClass().getMethod("getDelegate");
            Object delegate = getDelegateMethod.invoke(slot);
            if (delegate instanceof Slot) {
                return delegate;
            }
            
            // Try to get original slot field
            java.lang.reflect.Field slotField = slot.getClass().getDeclaredField("slot");
            slotField.setAccessible(true);
            Object originalSlot = slotField.get(slot);
            if (originalSlot instanceof Slot) {
                return originalSlot;
            }
        } catch (Exception ignored) {
            // If original Slot can't be obtained, return null
        }
        return null;
    }
}
