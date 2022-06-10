package com.cleanroommc.invtweaks.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemCompareHelper {

    public static String getMod(ItemStack item) {
        ResourceLocation loc = item.getItem().getRegistryName();
        if (loc == null) throw new IllegalStateException("Item doesn't have a registry name!");
        return loc.getNamespace();
    }

    public static String getId(ItemStack item) {
        ResourceLocation loc = item.getItem().getRegistryName();
        if (loc == null) throw new IllegalStateException("Item doesn't have a registry name!");
        return loc.getPath();
    }

    public static int getMeta(ItemStack item) {
        return item.getMetadata();
    }

    public static NBTTagCompound getNbt(ItemStack item) {
        return item.getTagCompound();
    }

    public static int compareMod(ItemStack stack1, ItemStack stack2) {
        return getMod(stack1).compareTo(getMod(stack2));
    }

    public static int compareId(ItemStack stack1, ItemStack stack2) {
        return getId(stack1).compareTo(getId(stack2));
    }

    public static int compareDisplayName(ItemStack stack1, ItemStack stack2) {
        return stack1.getDisplayName().compareTo(stack2.getDisplayName());
    }

    public static int compareMeta(ItemStack stack1, ItemStack stack2) {
        return Integer.compare(getMeta(stack1), getMeta(stack2));
    }

    public static int compareCount(ItemStack stack1, ItemStack stack2) {
        return Integer.compare(stack1.getCount(), stack2.getCount());
    }

    public static int compareOreDict(ItemStack stack1, ItemStack stack2) {
        Set<String> ores1 = OreDictHelper.getOreDicts(stack1);
        Set<String> ores2 = OreDictHelper.getOreDicts(stack2);
        if (ores1.isEmpty() && ores2.isEmpty()) return 0;
        List<String> ores3 = new ArrayList<>();
        List<String> ores4 = new ArrayList<>();
        for (String oreDict : ores1) {
            if (!ores2.contains(oreDict)) {
                ores3.add(oreDict);
            }
        }
        for (String oreDict : ores2) {
            if (!ores1.contains(oreDict)) {
                ores4.add(oreDict);
            }
        }
        if (ores3.size() != ores4.size()) {
            return Integer.compare(ores3.size(), ores4.size());
        }
        if (ores3.size() != 1) return 0;
        return ores3.get(0).compareTo(ores4.get(0));
    }

    public static int compareHasNbt(ItemStack stack1, ItemStack stack2) {
        NBTTagCompound nbt1 = stack1.getTagCompound();
        NBTTagCompound nbt2 = stack2.getTagCompound();
        if (nbt1 == null && nbt2 == null) return 0;
        if (nbt1 == null) return -1;
        if (nbt2 == null) return 1;
        return compareNotNullNbt(nbt1, nbt2);
    }

    public static int compareNotNullNbt(@NotNull NBTTagCompound nbt1, @NotNull NBTTagCompound nbt2) {
        int result = compareNbtSize(nbt1, nbt2);
        if (result != 0) return result;
        return compareNbtValues(nbt1, nbt2);
    }

    public static int compareNbtValues(@NotNull NBTTagCompound nbt1, @NotNull NBTTagCompound nbt2) {
        int total = 0;
        for (String key : nbt1.getKeySet()) {
            if (nbt2.hasKey(key)) {
                int result = compareNbtBase(nbt1.getTag(key), nbt2.getTag(key));
                total += result;
            }
        }
        return MathHelper.clamp(total, -1, 1);
    }

    public static int compareNbtBase(NBTBase nbt1, NBTBase nbt2) {
        if (nbt1.getId() != nbt2.getId()) return 0;
        if (nbt1.getId() == Constants.NBT.TAG_COMPOUND) {
            return compareNbtValues((NBTTagCompound) nbt1, (NBTTagCompound) nbt2);
        }
        if (nbt1 instanceof NBTPrimitive) {
            return Double.compare(((NBTPrimitive) nbt1).getDouble(), ((NBTPrimitive) nbt2).getDouble());
        }
        if (nbt1.getId() == Constants.NBT.TAG_BYTE_ARRAY) {
            byte[] array1 = ((NBTTagByteArray) nbt1).getByteArray();
            byte[] array2 = ((NBTTagByteArray) nbt2).getByteArray();
            if (array1.length != array2.length) {
                return array1.length < array2.length ? -1 : 1;
            }
            int total = 0;
            for (int i = 0; i < array1.length; i++) {
                total += Byte.compare(array1[i], array2[i]);
            }
            return total;
        }
        if (nbt1.getId() == Constants.NBT.TAG_INT_ARRAY) {
            int[] array1 = ((NBTTagIntArray) nbt1).getIntArray();
            int[] array2 = ((NBTTagIntArray) nbt2).getIntArray();
            if (array1.length != array2.length) {
                return array1.length < array2.length ? -1 : 1;
            }
            int total = 0;
            for (int i = 0; i < array1.length; i++) {
                total += Integer.compare(array1[i], array2[i]);
            }
            return total;
        }
        if (nbt1.getId() == Constants.NBT.TAG_LONG_ARRAY) {
            // TODO for some fucking reason long array tag doesn't have a array getter
            return 0;
        }
        return 0;
    }

    public static int compareNbtSize(@NotNull NBTTagCompound nbt1, @NotNull NBTTagCompound nbt2) {
        if (nbt1.getSize() < nbt2.getSize()) return -1;
        if (nbt1.getSize() > nbt2.getSize()) return 1;
        List<NBTTagCompound> subTags1 = new ArrayList<>();
        List<NBTTagCompound> subTags2 = new ArrayList<>();
        subTags1.add(nbt1);
        subTags2.add(nbt2);
        while (true) {
            subTags1 = getAllSubTags(subTags1);
            subTags2 = getAllSubTags(subTags2);
            int size1 = getTotalSubTags(subTags1);
            int size2 = getTotalSubTags(subTags2);
            if (size1 == 0 && size2 == 0) return 0;
            if (size1 < size2) return -1;
            if (size1 > size2) return 1;
        }
    }

    private static List<NBTTagCompound> getAllSubTags(List<NBTTagCompound> tags) {
        List<NBTTagCompound> subTags = new ArrayList<>();
        for (NBTTagCompound nbt : tags) {
            for (String key : nbt.getKeySet()) {
                NBTBase nbtBase = nbt.getTag(key);
                if (nbt.getTag(key) instanceof NBTTagCompound) {
                    subTags.add((NBTTagCompound) nbtBase);
                }
            }
        }
        return subTags;
    }

    private static int getTotalSubTags(List<NBTTagCompound> tags) {
        int sum = 0;
        for (NBTTagCompound nbt : tags) {
            sum += nbt.getSize();
        }
        return sum;
    }
}
