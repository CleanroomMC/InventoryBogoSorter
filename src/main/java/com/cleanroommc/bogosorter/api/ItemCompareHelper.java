package com.cleanroommc.bogosorter.api;

import com.cleanroommc.bogosorter.OreDictHelper;
import com.cleanroommc.bogosorter.sort.SortHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        if ((nbt1 == null) == (nbt2 == null)) return 0;
        if (nbt1 == null) return -1;
        return 1;
    }

    public static int compareNotNullNbt(@NotNull NBTTagCompound nbt1, @NotNull NBTTagCompound nbt2) {
        int result = compareNbtSize(nbt1, nbt2);
        if (result != 0) return result;
        return compareNbtValues(nbt1, nbt2);
    }

    public static int compareNbtValues(ItemStack itemStack1, ItemStack itemStack2) {
        int result = compareHasNbt(itemStack1, itemStack2);
        if (result != 0) return result;
        return itemStack1.hasTagCompound() ? compareNbtValues(itemStack1.getTagCompound(), itemStack2.getTagCompound()) : 0;
    }

    public static int compareNbtValues(@NotNull NBTTagCompound nbt1, @NotNull NBTTagCompound nbt2) {
        int result = 0;
        for (NbtSortRule nbtSortRule : SortHandler.getNbtSortRules()) {
            result = nbtSortRule.compare(nbt1, nbt2);
            if (result != 0) return result;
        }
        return result;
    }

    public static int compareNbtAllValues(ItemStack itemStack1, ItemStack itemStack2) {
        int result = compareHasNbt(itemStack1, itemStack2);
        if (result != 0) return result;
        return itemStack1.hasTagCompound() ? compareNbtAllValues(itemStack1.getTagCompound(), itemStack2.getTagCompound()) : 0;
    }

    public static int compareNbtAllValues(@NotNull NBTTagCompound nbt1, @NotNull NBTTagCompound nbt2) {
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
            return compareNbtAllValues((NBTTagCompound) nbt1, (NBTTagCompound) nbt2);
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

    public static int compareNbtSize(ItemStack itemStack1, ItemStack itemStack2) {
        int result = compareHasNbt(itemStack1, itemStack2);
        if (result != 0) return result;
        return itemStack1.hasTagCompound() ? compareNbtSize(itemStack1.getTagCompound(), itemStack2.getTagCompound()) : 0;
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

    @Nullable
    public static NBTBase findSubTag(String path, NBTBase tag) {
        if (tag == null || path == null || path.isEmpty()) return null;
        String[] parts = path.split("/");
        for (String part : parts) {
            if (tag == null || tag.getId() != 10) return null;
            tag = ((NBTTagCompound) tag).getTag(part);
        }
        return tag;
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

    public static int comparePotionId(String potion1, String potion2) {
        String id1 = potion1.startsWith("strong") || potion1.startsWith("long") ? potion1.substring(potion1.indexOf('_') + 1) : potion1;
        String id2 = potion2.startsWith("strong") || potion2.startsWith("long") ? potion2.substring(potion2.indexOf('_') + 1) : potion2;
        int result = id1.compareTo(id2);
        if (result != 0) return result;
        boolean strong1 = potion1.startsWith("strong");
        boolean strong2 = potion2.startsWith("strong");
        if (strong1 && !strong2) return 1;
        if (!strong1 && strong2) return -1;
        return Boolean.compare(potion1.startsWith("long"), potion2.startsWith("long"));
    }

    public static int compareEnchantments(NBTTagList enchantments1, NBTTagList enchantments2) {
        int total1 = 0;
        for (NBTBase nbtBase : enchantments1) {
            NBTTagCompound nbt = (NBTTagCompound) nbtBase;
            total1 += nbt.getShort("id");
        }
        int total2 = 0;
        for (NBTBase nbtBase : enchantments2) {
            NBTTagCompound nbt = (NBTTagCompound) nbtBase;
            total2 += nbt.getShort("id");
        }
        int result = Integer.compare(total1, total2);
        if (result != 0) return result;
        total1 = 0;
        for (NBTBase nbtBase : enchantments1) {
            NBTTagCompound nbt = (NBTTagCompound) nbtBase;
            total1 += nbt.getShort("lvl");
        }
        total2 = 0;
        for (NBTBase nbtBase : enchantments2) {
            NBTTagCompound nbt = (NBTTagCompound) nbtBase;
            total2 += nbt.getShort("lvl");
        }
        return Integer.compare(total1, total2);
    }

    public static int compareEnchantment(NBTTagCompound enchantment1, NBTTagCompound enchantment2) {
        int result = Integer.compare(enchantment1.getShort("id"), enchantment2.getShort("id"));
        if (result != 0) return result;
        return Integer.compare(enchantment1.getShort("lvl"), enchantment2.getShort("lvl"));
    }

    public static int compareMaterial(ItemStack item1, ItemStack item2) {
        String mat1 = OreDictHelper.getMaterial(item1);
        String mat2 = OreDictHelper.getMaterial(item2);
        if (mat1 == null && mat2 == null) return 0;
        if (mat1 == null) return 1;
        return mat2 == null ? -1 : mat1.compareTo(mat2);
    }

    public static int compareOrePrefix(ItemStack item1, ItemStack item2) {
        String prefix = OreDictHelper.getOrePrefix(item1);
        String prefix1 = OreDictHelper.getOrePrefix(item2);
        if (prefix == null && prefix1 == null) return 0;
        if (prefix == null) return 1;
        if (prefix1 == null) return -1;
        return Integer.compare(OreDictHelper.getOrePrefixIndex(prefix), OreDictHelper.getOrePrefixIndex(prefix1));
    }
}
