package com.cleanroommc.bogosorter.common.refill;

import com.cleanroommc.bogosorter.BogoSorter;
import gregtech.api.items.IToolItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = BogoSorter.ID)
public class RefillHandler {

    private static final int[][] INVENTORY_PROXIMITY_MAP = {
            {27, 18, 9, 28, 19, 10, 29, 20, 11, 30, 21, 12, 31, 22, 13, 32, 23, 14, 33, 24, 15, 34, 25, 16, 35, 26, 17},
            {28, 19, 10, 27, 18, 9, 29, 20, 11, 30, 21, 12, 31, 22, 13, 32, 23, 14, 33, 24, 15, 34, 25, 16, 35, 26, 17},
            {29, 20, 11, 28, 19, 10, 30, 21, 12, 27, 18, 9, 31, 22, 13, 32, 23, 14, 33, 24, 15, 34, 25, 16, 35, 26, 17},
            {30, 21, 12, 29, 20, 11, 31, 22, 13, 28, 19, 10, 32, 23, 14, 27, 18, 9, 33, 24, 15, 34, 25, 16, 35, 26, 17},
            {31, 22, 13, 30, 21, 12, 32, 23, 14, 29, 20, 11, 33, 24, 15, 28, 19, 10, 34, 25, 16, 27, 18, 9, 35, 26, 17},
            {32, 23, 14, 31, 22, 13, 33, 24, 15, 30, 21, 12, 34, 25, 16, 29, 20, 11, 35, 26, 17, 28, 19, 10, 27, 18, 9},
            {33, 24, 15, 32, 23, 14, 34, 25, 16, 31, 22, 13, 35, 26, 17, 30, 21, 12, 29, 20, 11, 28, 19, 10, 27, 18, 9},
            {34, 25, 16, 33, 24, 15, 35, 26, 17, 32, 23, 14, 31, 22, 13, 30, 21, 12, 29, 20, 11, 28, 19, 10, 27, 18, 9},
            {35, 26, 17, 34, 25, 16, 33, 24, 15, 32, 23, 14, 31, 22, 13, 30, 21, 12, 29, 20, 11, 28, 19, 10, 27, 18, 9},
    };

    @SubscribeEvent
    public static void onDestroyItem(PlayerDestroyItemEvent event) {
        handleRefill(event.getEntityPlayer().inventory.currentItem, event.getOriginal(), event.getEntityPlayer().inventory);
    }

    public static void handleRefill(int hotbarIndex, ItemStack brokenItem, InventoryPlayer inventoryPlayer) {
        int[] slots = INVENTORY_PROXIMITY_MAP[hotbarIndex];
        for (int slot : slots) {
            ItemStack found = inventoryPlayer.mainInventory.get(slot);
            if (found.isEmpty()) continue;
            boolean matches = isMatch(brokenItem, found);
            if (matches) {
                Minecraft.getMinecraft()
						 .getSoundHandler()
						 .playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F));

                inventoryPlayer.mainInventory.set(hotbarIndex, found);
                inventoryPlayer.mainInventory.set(slot, ItemStack.EMPTY);
                return;
            }
        }
    }

	private static boolean isMatch(ItemStack brokenItem, ItemStack foundItem) {
        if (brokenItem.getItem() instanceof ItemBlock) {
            return ItemStack.areItemsEqual(brokenItem, foundItem) &&
                ItemStack.areItemStackTagsEqual(brokenItem, foundItem);
        } else if (brokenItem.getItem() instanceof ItemTool) {
            return ItemStack.areItemsEqualIgnoreDurability(brokenItem, foundItem);
        } else if ((BogoSorter.LOADED_MODS.get("gtce") || BogoSorter.LOADED_MODS.get("gtceu")) && brokenItem.getItem() instanceof IToolItem) {
            return ItemStack.areItemsEqual(brokenItem, foundItem);
        } else {
            // everything else (redstone, glowstone, etc)
            return ItemStack.areItemsEqual(brokenItem, foundItem);
        }
	}
}
