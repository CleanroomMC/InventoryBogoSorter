package com.cleanroommc.bogosorter.common.refill;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.network.NetworkUtils;
import gregtech.api.items.IToolItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiPredicate;

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
        if (shouldHandleRefill(event.getEntityPlayer(), event.getOriginal())) {
            new RefillHandler(event.getEntityPlayer().inventory.currentItem, event.getOriginal(), event.getEntityPlayer()).handleRefill();
        }
    }

    public static boolean shouldHandleRefill(EntityPlayer player, ItemStack brokenItem) {
        Container container = player.openContainer;
        return !NetworkUtils.isClient(player) && (container == null || container == player.inventoryContainer) && !brokenItem.isEmpty();
    }

    private BiPredicate<ItemStack, ItemStack> similarItemMatcher = (stack, stack2) -> stack.getItem() == stack2.getItem() && stack.getMetadata() == stack2.getMetadata();
    private BiPredicate<ItemStack, ItemStack> exactItemMatcher = RefillHandler::matchTags;
    private final int hotbarIndex;
    private final int[] slots;
    private final ItemStack brokenItem;
    private final EntityPlayer player;
    private final InventoryPlayer inventory;
    private final boolean swapItems;

    public RefillHandler(int hotbarIndex, ItemStack brokenItem, EntityPlayer player, boolean swapItems) {
        this.hotbarIndex = hotbarIndex;
        this.slots = INVENTORY_PROXIMITY_MAP[hotbarIndex];
        this.brokenItem = brokenItem;
        this.player = player;
        this.inventory = player.inventory;
        this.swapItems = swapItems;
    }

    public RefillHandler(int hotbarIndex, ItemStack brokenItem, EntityPlayer player) {
        this(hotbarIndex, brokenItem, player, false);
    }


    public boolean handleRefill() {
        if (!BogoSorterConfig.enableAutoRefill) return false;

        if (brokenItem.getItem() instanceof ItemBlock) {
            return findItem(false);
        } else if (brokenItem.isItemStackDamageable()) {
            similarItemMatcher = (stack, stack2) -> stack.getItem() == stack2.getItem();
            return findNormalDamageable();
        } else if ((BogoSorter.isGTCELoaded() || BogoSorter.isGTCEuLoaded()) && brokenItem.getItem() instanceof IToolItem) {
            exactItemMatcher = (stack, stack2) -> {
                if (stack.hasTagCompound() != stack2.hasTagCompound()) return false;
                if (!stack.hasTagCompound()) return true;
                return getToolMaterial(stack).equals(getToolMaterial(stack2));
            };
            return findNormalDamageable();
        } else {
            return findItem(true);
        }
    }

    private boolean findItem(boolean exactOnly) {
        ItemStack firstItemMatch = null;
        int firstItemMatchSlot = -1;
        for (int slot : slots) {
            ItemStack found = inventory.mainInventory.get(slot);
            if (found.isEmpty()) continue;
            if (similarItemMatcher.test(brokenItem, found)) {
                if (exactItemMatcher.test(brokenItem, found)) {
                    refillItem(found, slot);
                    return true;
                }
                if (firstItemMatch == null) {
                    firstItemMatch = found;
                    firstItemMatchSlot = slot;
                }
            }
        }
        if (firstItemMatch != null && !exactOnly) {
            refillItem(firstItemMatch, firstItemMatchSlot);
            return true;
        }
        return false;
    }

    private boolean findNormalDamageable() {
        if (findItem(false)) {
            return true;
        }

        Set<String> brokenToolClasses = brokenItem.getItem().getToolClasses(brokenItem);
        if (brokenToolClasses.isEmpty())
            return false;
        // try match tool type
        for (int slot : slots) {
            ItemStack found = inventory.mainInventory.get(slot);
            if (found.isEmpty()) continue;
            Set<String> toolTypes = found.getItem().getToolClasses(found);
            if (brokenToolClasses.equals(toolTypes)) {
                refillItem(found, slot);
                return true;
            }
        }

        int brokenTools = brokenToolClasses.size();
        for (int slot : slots) {
            ItemStack found = inventory.mainInventory.get(slot);
            if (found.isEmpty()) continue;
            Set<String> toolTypes = found.getItem().getToolClasses(found);
            int tools = toolTypes.size();
            if (tools == 0 || tools == brokenTools) continue;
            if (tools > brokenTools) {
                if (toolTypes.containsAll(brokenToolClasses)) {
                    refillItem(found, slot);
                    return true;
                }
            } else {
                if (brokenToolClasses.containsAll(toolTypes)) {
                    refillItem(found, slot);
                    return true;
                }
            }
        }

        return false;
    }

    private void refillItem(ItemStack refill, int refillIndex) {
        if (!inventory.player.getEntityWorld().isRemote) {
            Minecraft.getMinecraft()
                    .getSoundHandler()
                    .playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F));
        }

        inventory.mainInventory.set(hotbarIndex, refill);
        inventory.mainInventory.set(refillIndex, swapItems ? brokenItem.copy() : ItemStack.EMPTY);
        // if the replacing item is equal to the replaced item, it will not be synced
        // this makes sure that the sync is triggered
        player.inventoryContainer.inventoryItemStacks.set(hotbarIndex + 36, ItemStack.EMPTY);
    }

    private static boolean matchTags(ItemStack stackA, ItemStack stackB) {
        if (stackA.getTagCompound() == null && stackB.getTagCompound() != null) {
            return false;
        } else {
            return (stackA.getTagCompound() == null || stackA.getTagCompound().equals(stackB.getTagCompound())) && stackA.areCapsCompatible(stackB);
        }
    }

    // copy pasted and changed from gtce since i get errors when i try to use their method
    @NotNull
    private static String getToolMaterial(ItemStack itemStack) {
        NBTTagCompound statsTag = itemStack.getSubCompound("GT.ToolStats");
        if (statsTag == null) {
            return "";
        }
        String toolMaterialName;
        if (statsTag.hasKey("Material")) {
            toolMaterialName = statsTag.getString("Material");
        } else if (statsTag.hasKey("PrimaryMaterial")) {
            toolMaterialName = statsTag.getString("PrimaryMaterial");
        } else {
            return "";
        }
        return toolMaterialName;
    }
}
