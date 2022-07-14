package com.cleanroommc.bogosorter.common.refill;

import com.cleanroommc.bogosorter.BogoSorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Set;

@Mod.EventBusSubscriber(modid = BogoSorter.ID)
public class RefillHandler {

    private static final int[][] INVENTORY_PROXIMITY_MAP = {
            {1, 2, 3, 4, 5, 6, 7, 8, 27, 18, 9, 28, 19, 10, 29, 20, 11, 30, 21, 12, 31, 22, 13, 32, 23, 14, 33, 24, 15, 34, 25, 16, 35, 26, 17},
            {0, 2, 3, 4, 5, 6, 7, 8, 28, 19, 10, 27, 18, 9, 29, 20, 11, 30, 21, 12, 31, 22, 13, 32, 23, 14, 33, 24, 15, 34, 25, 16, 35, 26, 17},
            {1, 3, 0, 4, 5, 6, 7, 8, 29, 20, 11, 28, 19, 10, 30, 21, 12, 27, 18, 9, 31, 22, 13, 32, 23, 14, 33, 24, 15, 34, 25, 16, 35, 26, 17},
            {2, 4, 1, 5, 0, 6, 7, 8, 30, 21, 12, 29, 20, 11, 31, 22, 13, 28, 19, 10, 32, 23, 14, 27, 18, 9, 33, 24, 15, 34, 25, 16, 35, 26, 17},
            {3, 5, 2, 6, 1, 7, 0, 8, 31, 22, 13, 30, 21, 12, 32, 23, 14, 29, 20, 11, 33, 24, 15, 28, 19, 10, 34, 25, 16, 27, 18, 9, 35, 26, 17},
            {4, 6, 3, 7, 2, 8, 1, 0, 32, 23, 14, 31, 22, 13, 33, 24, 15, 30, 21, 12, 34, 25, 16, 29, 20, 11, 35, 26, 17, 28, 19, 10, 27, 18, 9},
            {5, 7, 4, 8, 3, 2, 1, 0, 33, 24, 15, 32, 23, 14, 34, 25, 16, 31, 22, 13, 35, 26, 17, 30, 21, 12, 29, 20, 11, 28, 19, 10, 27, 18, 9},
            {6, 8, 5, 4, 3, 2, 1, 0, 34, 25, 16, 33, 24, 15, 35, 26, 17, 32, 23, 14, 31, 22, 13, 30, 21, 12, 29, 20, 11, 28, 19, 10, 27, 18, 9},
            {7, 6, 5, 4, 3, 2, 1, 0, 35, 26, 17, 34, 25, 16, 33, 24, 15, 32, 23, 14, 31, 22, 13, 30, 21, 12, 29, 20, 11, 28, 19, 10, 27, 18, 9},
    };

    @SubscribeEvent
    public static void onDestroyItem(PlayerDestroyItemEvent event) {
        Container container = event.getEntityPlayer().openContainer;
        if ((container == null || container == event.getEntityPlayer().inventoryContainer) && !event.getOriginal().isEmpty()) {
            handleRefill(event.getEntityPlayer().inventory.currentItem, event.getOriginal(), event.getEntityPlayer().inventory);
        }
    }

    public static void handleRefill(int hotbarIndex, ItemStack brokenItem, InventoryPlayer inventoryPlayer) {
        int[] slots = INVENTORY_PROXIMITY_MAP[hotbarIndex];
        if (brokenItem.getItem() instanceof ItemBlock) {
            findBlockItem(slots, hotbarIndex, brokenItem, inventoryPlayer);
        } else if (brokenItem.isItemStackDamageable()) {
            findNormalDamageable(slots, hotbarIndex, brokenItem, inventoryPlayer);
        }
    }

    private static void findBlockItem(int[] slots, int hotbarIndex, ItemStack brokenItem, InventoryPlayer inventoryPlayer) {
        for (int slot : slots) {
            ItemStack found = inventoryPlayer.mainInventory.get(slot);
            if (found.isEmpty()) continue;
            if (brokenItem.getItem() == found.getItem() &&
                    brokenItem.getMetadata() == found.getMetadata() &&
                    matchTags(found, brokenItem)) {
                refillItem(found, inventoryPlayer, hotbarIndex, slot);
                return;
            }
        }
    }

    private static void findNormalDamageable(int[] slots, int hotbarIndex, ItemStack brokenItem, InventoryPlayer inventoryPlayer) {
        ItemStack firstItemMatch = null;
        int firstItemMatchSlot = -1;
        // try match item exact
        for (int slot : slots) {
            ItemStack found = inventoryPlayer.mainInventory.get(slot);
            if (found.isEmpty()) continue;
            if (brokenItem.getItem() == found.getItem()) {
                if (matchTags(brokenItem, found)) {
                    refillItem(found, inventoryPlayer, hotbarIndex, slot);
                    return;
                }
                if (firstItemMatch == null) {
                    firstItemMatch = found;
                    firstItemMatchSlot = slot;
                }
            }
        }
        // did found matching item, but nbt was different
        if (firstItemMatch != null) {
            refillItem(firstItemMatch, inventoryPlayer, hotbarIndex, firstItemMatchSlot);
            return;
        }
        Set<String> brokenToolClasses = brokenItem.getItem().getToolClasses(brokenItem);
        if (brokenToolClasses.isEmpty()) return;
        // try match tool type
        for (int slot : slots) {
            ItemStack found = inventoryPlayer.mainInventory.get(slot);
            if (found.isEmpty()) continue;
            Set<String> toolTypes = found.getItem().getToolClasses(found);
            if (brokenToolClasses.equals(toolTypes)) {
                refillItem(found, inventoryPlayer, hotbarIndex, slot);
                return;
            }
        }

        int brokenTools = brokenToolClasses.size();
        for (int slot : slots) {
            ItemStack found = inventoryPlayer.mainInventory.get(slot);
            if (found.isEmpty()) continue;
            Set<String> toolTypes = found.getItem().getToolClasses(found);
            int tools = toolTypes.size();
            if (tools == 0 || tools == brokenTools) continue;
            if (tools > brokenTools) {
                if (toolTypes.containsAll(brokenToolClasses)) {
                    refillItem(found, inventoryPlayer, hotbarIndex, slot);
                    return;
                }
            } else {
                if (brokenToolClasses.containsAll(toolTypes)) {
                    refillItem(found, inventoryPlayer, hotbarIndex, slot);
                    return;
                }
            }
        }
    }

    private static void refillItem(ItemStack refill, InventoryPlayer inventory, int hotbarIndex, int refillIndex) {
        if (!inventory.player.getEntityWorld().isRemote) {
            Minecraft.getMinecraft()
                    .getSoundHandler()
                    .playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F));
        }

        inventory.mainInventory.set(hotbarIndex, refill.copy());
        inventory.mainInventory.set(refillIndex, ItemStack.EMPTY);
    }

    private static boolean matchTags(ItemStack stackA, ItemStack stackB) {
        if (stackA.getTagCompound() == null && stackB.getTagCompound() != null) {
            return false;
        } else {
            return (stackA.getTagCompound() == null || stackA.getTagCompound().equals(stackB.getTagCompound())) && stackA.areCapsCompatible(stackB);
        }
    }
}
