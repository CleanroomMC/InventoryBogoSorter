package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.common.network.CDropSlots;
import com.cleanroommc.bogosorter.common.network.CShortcut;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class ShortcutHandler {

    public static boolean moveSingleItem(GuiContainer guiContainer, boolean emptySlot) {
        Slot slot = guiContainer.getSlotUnderMouse();
        if (slot == null || slot.getStack().isEmpty()) return false;
        NetworkHandler.sendToServer(new CShortcut(emptySlot ? CShortcut.Type.MOVE_SINGLE_EMPTY : CShortcut.Type.MOVE_SINGLE, slot.slotNumber));
        return true;
    }

    public static boolean moveSingleItem(EntityPlayer player, Container container, Slot slot, boolean emptySlot) {
        if (slot == null || slot.getStack().isEmpty()) return false;
        ItemStack stack = slot.getStack();
        ItemStack toInsert = stack.copy();
        toInsert.setCount(1);

        if (BogoSortAPI.isValidSortable(container)) {
            GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);

            SlotGroup slots = sortingContext.getSlotGroup(slot.slotNumber);
            SlotGroup otherSlots = BogoSortAPI.isPlayerSlot(slot) ? sortingContext.getNonPlayerSlotGroup() : sortingContext.getPlayerSlotGroup();
            if (otherSlots == null || slots == otherSlots) return false;

            toInsert = emptySlot ? insertToSlots(otherSlots.getSlots(), toInsert, true) : insertToSlots(otherSlots.getSlots(), toInsert);
        } else {
            List<Slot> otherSlots = new ArrayList<>();
            boolean isPlayer = BogoSortAPI.isPlayerSlot(slot);
            for (Slot slot1 : container.inventorySlots) {
                if (isPlayer != BogoSortAPI.isPlayerSlot(slot1)) {
                    otherSlots.add(slot1);
                }
            }
            if (!emptySlot && toInsert.isStackable()) {
                toInsert = insertToSlots(otherSlots, toInsert, false);
            }
            if (!toInsert.isEmpty()) {
                toInsert = insertToSlots(otherSlots, toInsert, true);
            }
        }
        if (toInsert.isEmpty()) {
            toInsert = stack.copy();
            toInsert.shrink(1);
            slot.putStack(toInsert);
            return true;
        }
        return false;
    }

    public static boolean moveAllItems(GuiContainer guiContainer, boolean sameItemOnly) {
        Container container = guiContainer.inventorySlots;
        Slot slot = guiContainer.getSlotUnderMouse();
        if (slot == null || !BogoSortAPI.isValidSortable(container) || (sameItemOnly && slot.getStack().isEmpty()))
            return false;
        NetworkHandler.sendToServer(new CShortcut(sameItemOnly ? CShortcut.Type.MOVE_ALL_SAME : CShortcut.Type.MOVE_ALL, slot.slotNumber));
        return true;
    }

    public static boolean moveAllItems(EntityPlayer player, Container container, Slot slot, boolean sameItemOnly) {
        if (slot == null || !BogoSortAPI.isValidSortable(container)) return false;
        ItemStack stack = slot.getStack().copy();
        if (sameItemOnly && stack.isEmpty()) return false;
        GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);

        SlotGroup slots = sortingContext.getSlotGroup(slot.slotNumber);
        SlotGroup otherSlots = BogoSortAPI.isPlayerSlot(slot) ? sortingContext.getNonPlayerSlotGroup() : sortingContext.getPlayerSlotGroup();
        if (slots == null || otherSlots == null || slots == otherSlots) return false;
        for (Slot slot1 : slots.getSlots()) {
            ItemStack stackInSlot = slot1.getStack();
            ItemStack copy = stackInSlot.copy();
            if (stackInSlot.isEmpty() || (sameItemOnly && !ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)))
                continue;
            ItemStack remainder = insertToSlots(otherSlots.getSlots(), copy);
            int inserted = stackInSlot.getCount() - remainder.getCount();
            if (inserted > 0) {
                slot1.putStack(remainder.copy());
            }
        }
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

        SlotGroup slots = GuiSortingContext.getOrCreate(container).getSlotGroup(slot.slotNumber);
        if (slots == null || !slots.isPlayerInventory()) return false;
        IntList slotsToDrop = new IntArrayList();
        for (Slot slot1 : slots.getSlots()) {
            ItemStack stack = slot1.getStack();
            if (!stack.isEmpty() && (!onlySameType || ItemHandlerHelper.canItemStacksStack(stack, item))) {
                slotsToDrop.add(slot1.slotNumber);
            }
        }
        if (slotsToDrop.isEmpty()) return false;
        NetworkHandler.sendToServer(new CDropSlots(slotsToDrop));
        return true;
    }

    private static ItemStack insertToSlots(List<Slot> slots, ItemStack stack) {
        if (stack.isStackable()) {
            stack = insertToSlots(slots, stack, false);
        }
        if (!stack.isEmpty()) {
            stack = insertToSlots(slots, stack, true);
        }
        return stack;
    }

    private static ItemStack insertToSlots(List<Slot> slots, ItemStack stack, boolean emptyOnly) {
        for (Slot slot : slots) {
            stack = insert(slot, stack, emptyOnly);
            if (stack.isEmpty()) return stack;
        }
        return stack;
    }

    private static ItemStack insert(Slot slot, ItemStack stack, boolean emptyOnly) {
        ItemStack stackInSlot = slot.getStack();
        if (emptyOnly) {
            if (!stackInSlot.isEmpty() || !slot.isItemValid(stack)) return stack;
            int amount = Math.min(stack.getCount(), slot.getItemStackLimit(stack));
            if (amount <= 0) return stack;
            ItemStack newStack = stack.copy();
            newStack.setCount(amount);
            stack.shrink(amount);
            slot.putStack(newStack);
            return stack.isEmpty() ? ItemStack.EMPTY : stack;
        }
        if (!stackInSlot.isEmpty() && ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)) {
            int amount = Math.min(slot.getItemStackLimit(stackInSlot), Math.min(stack.getCount(), stackInSlot.getMaxStackSize() - stackInSlot.getCount()));
            if (amount <= 0) return stack;
            stack.shrink(amount);
            stackInSlot.grow(amount);
            return stack.isEmpty() ? ItemStack.EMPTY : stack;
        }
        return stack;
    }
}
