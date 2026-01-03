package com.cleanroommc.bogosorter.common.lock;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface LockSlotCapability extends INBTSerializable<NBTTagLong> {

    ResourceLocation ID = new ResourceLocation(BogoSorter.ID, "favorite_slot_cap");

    static LockSlotCapability getForPlayer(EntityPlayer player) {
        return player == null ? UNLOCKED : Objects.requireNonNull(player.getCapability(BogoSorter.favoriteSlotCap, null), () -> "Favorite slot cap is not present on player");
    }

    long getLockedSlots();

    void setLockedSlots(long lockedSlots);

    default boolean isSlotLocked(int index) {
        return (getLockedSlots() & (1L << index)) != 0;
    }

    default void setSlotLocked(int index, boolean value) {
        if (!value) setLockedSlots(getLockedSlots() & ~(1L << index));
        else setLockedSlots(getLockedSlots() | (1L << index));
    }

    default void toggleLock(int index) {
        setSlotLocked(index, !isSlotLocked(index));
    }

    default boolean isSlotLocked(Slot slot) {
        return isSlotLocked((ISlot) slot);
    }

    default boolean isSlotLocked(ISlot slot) {
        return BogoSortAPI.isPlayerSlot(slot) && isSlotLocked(slot.bogo$getSlotIndex());
    }

    @Override
    default NBTTagLong serializeNBT() {
        return new NBTTagLong(getLockedSlots());
    }

    @Override
    default void deserializeNBT(NBTTagLong nbt) {
        setLockedSlots(nbt.getLong());
    }

    class Storage implements Capability.IStorage<LockSlotCapability> {

        @Override
        public @Nullable NBTBase writeNBT(Capability<LockSlotCapability> capability, LockSlotCapability instance, EnumFacing side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<LockSlotCapability> capability, LockSlotCapability instance, EnumFacing side, NBTBase nbt) {
            instance.deserializeNBT((NBTTagLong) nbt);
        }
    }

    class Default implements LockSlotCapability {

        private long lockedSlots;

        @Override
        public long getLockedSlots() {
            return lockedSlots;
        }

        @Override
        public void setLockedSlots(long lockedSlots) {
            this.lockedSlots = lockedSlots;
            // force sorting context to be recreated to consider new locked slots
            GuiSortingContext.invalidateCurrent();
        }
    }

    class Provider implements ICapabilityProvider, INBTSerializable<NBTTagLong> {

        private final LockSlotCapability cap = new Default();

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == BogoSorter.favoriteSlotCap;
        }

        @Override
        public @Nullable <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == BogoSorter.favoriteSlotCap ? BogoSorter.favoriteSlotCap.cast(cap) : null;
        }

        @Override
        public NBTTagLong serializeNBT() {
            return cap.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagLong nbt) {
            cap.deserializeNBT(nbt);
        }
    }

    LockSlotCapability UNLOCKED = new LockSlotCapability() {
        @Override
        public long getLockedSlots() {
            return 0L;
        }

        @Override
        public void setLockedSlots(long lockedSlots) {}

        @Override
        public boolean isSlotLocked(int index) {
            return false;
        }

        @Override
        public void setSlotLocked(int index, boolean value) {}
    };
}
