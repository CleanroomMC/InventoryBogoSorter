package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.common.network.CShortcut;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class ShortcutHandler {

    @SideOnly(Side.CLIENT)
    public static boolean moveSingleItem(GuiContainer guiContainer, boolean emptySlot) {
        Slot slot = guiContainer.getSlotUnderMouse();
        if (slot == null || slot.getStack().isEmpty()) return false;
        NetworkHandler.sendToServer(new CShortcut(emptySlot ? CShortcut.Type.MOVE_SINGLE_EMPTY : CShortcut.Type.MOVE_SINGLE, slot.slotNumber));
        return true;
    }

    public static void moveSingleItem(EntityPlayer player, Container container, ISlot slot, boolean emptySlot) {
        moveItemStack(player, container, slot, emptySlot, 1);
    }

    @SideOnly(Side.CLIENT)
    public static boolean moveItemStack(GuiContainer guiContainer, boolean emptySlot) {
        Slot slot = guiContainer.getSlotUnderMouse();
        if (slot == null || slot.getStack().isEmpty()) return false;

        EntityPlayer player = Minecraft.getMinecraft().player;
        Container container = guiContainer.inventorySlots;
        ISlot iSlot = BogoSortAPI.INSTANCE.getSlot(slot);

        moveItemStack(player, container, iSlot, emptySlot, iSlot.bogo$getStack().getCount());
        return true;
    }


    public static void moveItemStack(EntityPlayer player, Container container, ISlot slot, boolean emptySlot, int amount) {
        if (slot == null || slot.bogo$getStack().isEmpty()) return;
        ItemStack stack = slot.bogo$getStack();
        amount = Math.min(amount, stack.getMaxStackSize());
        ItemStack toInsert = stack.copy();
        toInsert.setCount(amount);

        if (BogoSortAPI.isValidSortable(container)) {
            GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);

            SlotGroup slots = sortingContext.getSlotGroup(slot.bogo$getSlotNumber());
            SlotGroup otherSlots = BogoSortAPI.isPlayerSlot(slot) ? sortingContext.getNonPlayerSlotGroup() : sortingContext.getPlayerSlotGroup();
            if (otherSlots == null || slots == otherSlots) return;

            toInsert = emptySlot ?
                    BogoSortAPI.insert(container, otherSlots.getSlots(), toInsert, true) :
                    BogoSortAPI.insert(container, otherSlots.getSlots(), toInsert);
        } else {
            List<ISlot> otherSlots = new ArrayList<>();
            boolean isPlayer = BogoSortAPI.isPlayerSlot(slot);
            for (Slot slot1 : container.inventorySlots) {
                if (isPlayer != BogoSortAPI.isPlayerSlot(slot1)) {
                    otherSlots.add(BogoSortAPI.INSTANCE.getSlot(slot1));
                }
            }
            toInsert = emptySlot ?
                    BogoSortAPI.insert(container, otherSlots, toInsert, true) :
                    BogoSortAPI.insert(container, otherSlots, toInsert);
        }
        if (toInsert.isEmpty()) {
            toInsert = stack.copy();
            toInsert.shrink(amount);
            slot.bogo$putStack(toInsert);
            // needed for crafting tables
            slot.bogo$onSlotChanged(stack, toInsert);
            // I hope im doing this right
            toInsert = stack.copy();
            toInsert.setCount(amount);
            slot.bogo$onTake(player, toInsert);
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean moveAllItems(GuiContainer guiContainer, boolean sameItemOnly) {
        Container container = guiContainer.inventorySlots;
        Slot slot = guiContainer.getSlotUnderMouse();
        if (slot == null || !BogoSortAPI.isValidSortable(container)) return false;
        ISlot iSlot = BogoSortAPI.INSTANCE.getSlot(slot);
        if (sameItemOnly && iSlot.bogo$getStack().isEmpty())
            return false;
        NetworkHandler.sendToServer(new CShortcut(sameItemOnly ? CShortcut.Type.MOVE_ALL_SAME : CShortcut.Type.MOVE_ALL, iSlot.bogo$getSlotNumber()));
        return true;
    }

    public static void moveAllItems(EntityPlayer player, Container container, ISlot slot, boolean sameItemOnly) {
        if (slot == null || !BogoSortAPI.isValidSortable(container)) return;
        ItemStack stack = slot.bogo$getStack().copy();
        if (sameItemOnly && stack.isEmpty()) return;
        GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);

        SlotGroup slots = sortingContext.getSlotGroup(slot.bogo$getSlotNumber());
        SlotGroup otherSlots = BogoSortAPI.isPlayerSlot(slot) ? sortingContext.getNonPlayerSlotGroup() : sortingContext.getPlayerSlotGroup();
        if (slots == null || otherSlots == null || slots == otherSlots) return;
        for (ISlot slot1 : slots.getSlots()) {
            ItemStack stackInSlot = slot1.bogo$getStack();
            ItemStack copy = stackInSlot.copy();
            if (stackInSlot.isEmpty() || (sameItemOnly && !stackInSlot.isItemEqual(stack)))
                continue;
            ItemStack remainder = BogoSortAPI.insert(container, otherSlots.getSlots(), copy);
            int inserted = stackInSlot.getCount() - remainder.getCount();
            if (inserted > 0) {
                slot1.bogo$putStack(remainder.copy());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean dropItems(GuiContainer guiContainer, boolean onlySameType) {
        Slot slot = guiContainer.getSlotUnderMouse();
        if (slot == null || slot.getStack().isEmpty()) return false;
        if (!BogoSortAPI.isPlayerSlot(slot) && !BogoSortAPI.isValidSortable(guiContainer.inventorySlots)) return false;
        NetworkHandler.sendToServer(new CShortcut(onlySameType ? CShortcut.Type.DROP_ALL_SAME : CShortcut.Type.DROP_ALL, slot.slotNumber));
        return true;
    }

    public static void dropItems(EntityPlayer player, Container container, ISlot slot, boolean onlySameType) {
        ItemStack stack = slot.bogo$getStack();
        if (onlySameType && stack.isEmpty()) return;
        SlotGroup slots = GuiSortingContext.getOrCreate(container).getSlotGroup(slot.bogo$getSlotNumber());
        if (slots == null) return;
        for (ISlot slot1 : slots.getSlots()) {
            ItemStack stackInSlot = slot1.bogo$getStack();
            if (!stackInSlot.isEmpty() && (!onlySameType || stackInSlot.isItemEqual(stack))) {
                slot1.bogo$putStack(ItemStack.EMPTY);
                player.dropItem(stackInSlot, true);
            }
        }
        return;
    }

    public static ItemStack insertToSlots(List<ISlot> slots, ItemStack stack, boolean emptyOnly) {
        for (ISlot slot : slots) {
            stack = insert(slot, stack, emptyOnly);
            if (stack.isEmpty()) return stack;
        }
        return stack;
    }

    public static ItemStack insert(ISlot slot, ItemStack stack, boolean emptyOnly) {
        ItemStack stackInSlot = slot.bogo$getStack();
        if (emptyOnly) {
            if (!stackInSlot.isEmpty() || !slot.bogo$isItemValid(stack)) return stack;
            int amount = Math.min(stack.getCount(), slot.bogo$getItemStackLimit(stack));
            if (amount <= 0) return stack;
            ItemStack newStack = stack.copy();
            newStack.setCount(amount);
            stack.shrink(amount);
            slot.bogo$putStack(newStack);
            return stack.isEmpty() ? ItemStack.EMPTY : stack;
        }
        if (!stackInSlot.isEmpty() && ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)) {
            int amount = Math.min(slot.bogo$getItemStackLimit(stackInSlot), Math.min(stack.getCount(), slot.bogo$getMaxStackSize(stack) - stackInSlot.getCount()));
            if (amount <= 0) return stack;
            stack.shrink(amount);
            stackInSlot.grow(amount);
            slot.bogo$putStack(stackInSlot);
            return stack.isEmpty() ? ItemStack.EMPTY : stack;
        }
        return stack;
    }
}
