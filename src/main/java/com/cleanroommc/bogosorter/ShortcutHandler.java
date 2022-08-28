package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.common.network.CDropSlots;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class ShortcutHandler {

    public static boolean moveSingleItem(GuiContainer guiContainer) {
        Container container = guiContainer.inventorySlots;
        Slot slot = guiContainer.getSlotUnderMouse();
        if (slot == null || slot.getStack().isEmpty()) return false;
        ItemStack stack = slot.getStack();

        return false;
    }

    public static boolean moveAllItems(GuiContainer guiContainer) {
        Container container = guiContainer.inventorySlots;
        Slot slot = guiContainer.getSlotUnderMouse();
        if (slot == null || !BogoSortAPI.isValidSortable(container)) return false;

        GuiSortingContext sortingContext = GuiSortingContext.create(container);

        Slot[][] slots = sortingContext.getSlotGroup(slot.slotNumber);
        Slot[][] otherSlots = BogoSortAPI.isPlayerSlot(slot) ? sortingContext.getNonPlayerSlotGroup() : sortingContext.getPlayerSlotGroup();
        if (slots == null || otherSlots == null || slots == otherSlots) return false;

        for (Slot[] slotRow : slots) {
            for (Slot slot1 : slotRow) {
                ItemStack stack = slot1.getStack();
                if (stack.isEmpty()) continue;
                ItemStack remainder = insertToSlots(otherSlots, stack);
                slot1.putStack(remainder);
            }
        }

        SortHandler.sort(slots, true);
        SortHandler.sort(otherSlots, true);
        return true;
    }

    public static boolean dropItems(GuiContainer guiContainer, boolean onlySameType) {
        Container container = guiContainer.inventorySlots;
        Slot slot = guiContainer.getSlotUnderMouse();
        if (slot == null) return false;
        boolean player = BogoSortAPI.isPlayerSlot(slot);
        if (!player && !BogoSortAPI.isValidSortable(container)) return false;
        ItemStack item = slot.getStack();
        if (onlySameType && item.isEmpty()) return false;

        Slot[][] slots = GuiSortingContext.create(container, player).getSlotGroup(slot.slotNumber);
        if (slots == null) return false;
        IntList slotsToDrop = new IntArrayList();
        for (Slot[] slotRow : slots) {
            for (Slot slot1 : slotRow) {
                ItemStack stack = slot1.getStack();
                if (!stack.isEmpty() && (!onlySameType || ItemHandlerHelper.canItemStacksStack(stack, item))) {
                    slotsToDrop.add(slot1.slotNumber);
                }
            }
        }
        if (slotsToDrop.isEmpty()) return false;
        NetworkHandler.sendToServer(new CDropSlots(slotsToDrop));
        return true;
    }

    private static ItemStack insertToSlots(Slot[][] slots, ItemStack stack) {
        if (!stack.isStackable()) {
            return insertToEmptySlots(slots, stack);
        }
        stack = insertToSlotsStacked(slots, stack);
        if (!stack.isEmpty()) {
            stack = insertToEmptySlots(slots, stack);
        }
        return stack;
    }

    private static ItemStack insertToSlotsStacked(Slot[][] slots, ItemStack stack) {
        for (Slot[] slotRow : slots) {
            for (Slot slot : slotRow) {
                ItemStack stackInSlot = slot.getStack();
                if (stack.isEmpty()) continue;
                if (ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)) {
                    int amount = Math.min(stack.getCount(), stackInSlot.getMaxStackSize() - stackInSlot.getCount());
                    if (amount <= 0) continue;
                    stack.shrink(amount);
                    stackInSlot.grow(amount);
                    if (stack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return stack;
    }

    private static ItemStack insertToEmptySlots(Slot[][] slots, ItemStack stack) {
        for (Slot[] slotRow : slots) {
            for (Slot slot : slotRow) {
                if (slot.getStack().isEmpty()) {
                    slot.putStack(stack);
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }
}
