package com.cleanroommc.bogosorter.common;

import com.cleanroommc.bogosorter.common.network.CDropItems;
import com.cleanroommc.bogosorter.common.network.CSlotSync;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class McUtils {

    public static void dropItem(ItemStack stack, World world, double x, double y, double z) {
        dropItem(stack, world, x, y, z, null, false, 10);
    }

    public static void dropItem(ItemStack stack, World world, double x, double y, double z, @Nullable String thrower, boolean noDespawn, int pickUpDelay) {
        if (!world.isRemote && !stack.isEmpty() && !world.restoringBlockSnapshots) {
            double d0 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
            double d1 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
            double d2 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
            EntityItem entityitem = new EntityItem(world, x + d0, y + d1, z + d2, stack);
            entityitem.setPickupDelay(pickUpDelay);
            entityitem.setThrower(thrower);
            if (noDespawn) {
                entityitem.setNoDespawn();
            }
            world.spawnEntity(entityitem);
        }
    }

    public static void giveItemsToPlayer(EntityPlayer player, List<ItemStack> items) {
        if (player == null || items.isEmpty()) return;
        if (!player.world.isRemote) {
            giveItemsToPlayerServer(player, items);
            return;
        }
        CDropItems packet = new CDropItems(items);
        NetworkHandler.sendToServer(packet);
    }

    public static void giveItemsToPlayerServer(EntityPlayer player, List<ItemStack> items) {
        if (player == null || items.isEmpty() || player.world.isRemote) return;
        PlayerMainInvWrapper itemHandler = new PlayerMainInvWrapper(player.inventory);
        items.removeIf(item -> {
            ItemStack remainder = insertToPlayer(itemHandler, item, false);
            return remainder.isEmpty();
        });
        for (ItemStack item : items) {
            player.dropItem(item, false, false);
        }
    }

    public static ItemStack insertToPlayer(PlayerMainInvWrapper itemHandler, ItemStack stack, boolean simulate) {
        if (itemHandler == null || stack.isEmpty())
            return stack;

        // not stackable -> just insert into a new slot
        if (!stack.isStackable()) {
            return insertItem(itemHandler, stack, simulate, 9);
        }

        int sizeInventory = itemHandler.getSlots();

        // go through the inventory and try to fill up already existing items
        for (int i = 9; i < sizeInventory; i++) {
            ItemStack slot = itemHandler.getStackInSlot(i);
            if (ItemHandlerHelper.canItemStacksStackRelaxed(slot, stack)) {
                stack = itemHandler.insertItem(i, stack, simulate);

                if (stack.isEmpty()) {
                    break;
                }
            }
        }

        return insertItem(itemHandler, stack, simulate, 9);
    }

    public static ItemStack insertItem(IItemHandler dest, @Nonnull ItemStack stack, boolean simulate, int startSlot) {
        if (dest == null || stack.isEmpty())
            return stack;

        for (int i = startSlot; i < dest.getSlots(); i++) {
            stack = dest.insertItem(i, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    @SideOnly(Side.CLIENT)
    public static void syncSlotsToServer(Slot[][] slotGroup) {
        List<Pair<ItemStack, Integer>> syncSlots = new ArrayList<>();
        for (Slot[] slotRow : slotGroup) {
            for (Slot slot : slotRow) {
                syncSlots.add(Pair.of(slot.getStack(), slot.slotNumber));
            }
        }
        NetworkHandler.sendToServer(new CSlotSync(syncSlots));
    }

    @SideOnly(Side.CLIENT)
    public static void syncSlotsToServer(Iterable<Slot> slots) {
        List<Pair<ItemStack, Integer>> syncSlots = new ArrayList<>();
        for (Slot slot : slots) {
            syncSlots.add(Pair.of(slot.getStack(), slot.slotNumber));
        }
        NetworkHandler.sendToServer(new CSlotSync(syncSlots));
    }

    @SideOnly(Side.CLIENT)
    public static void syncSlotToServer(Slot slot) {
        List<Pair<ItemStack, Integer>> syncSlots = Collections.singletonList(Pair.of(slot.getStack(), slot.slotNumber));
        NetworkHandler.sendToServer(new CSlotSync(syncSlots));
    }
}
