package com.cleanroommc.invtweaks;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

@Mod.EventBusSubscriber(modid = InventoryTweaks.ID)
public class OreDictHelper {

    private static final Hash.Strategy<ItemStack> ITEM_HASH_STRATEGY = new Hash.Strategy<ItemStack>() {
        @Override
        public int hashCode(ItemStack o) {
            return Objects.hash(o.getItem(), o.getMetadata());
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == b) return true;
            if (a == null || b == null) return false;
            return (a.isEmpty() && b.isEmpty()) ||
                    (a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata());
        }
    };

    private static final Map<ItemStack, Set<String>> ORE_DICTS = new Object2ObjectOpenCustomHashMap<>(ITEM_HASH_STRATEGY);
    private static final Map<ItemStack, String> MATERIALS = new Object2ObjectOpenCustomHashMap<>(ITEM_HASH_STRATEGY);
    private static final Map<ItemStack, String> PREFIXES = new Object2ObjectOpenCustomHashMap<>(ITEM_HASH_STRATEGY);
    private static final Map<String, Integer> ORE_PREFIXES;

    @SubscribeEvent
    public static void onItemRegistration(OreDictionary.OreRegisterEvent event) {
        ORE_DICTS.computeIfAbsent(event.getOre(), key -> new HashSet<>()).add(event.getName());

        String oreName = event.getName();
        //and try to transform registration name into OrePrefix + Material pair
        if (!ORE_PREFIXES.containsKey(oreName)) {
            String material = null;
            String prefix = null;
            //split ore dict name to parts
            //oreBasalticMineralSand -> ore, Basaltic, Mineral, Sand
            ArrayList<String> splits = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            for (char character : oreName.toCharArray()) {
                if (Character.isUpperCase(character)) {
                    if (builder.length() > 0) {
                        splits.add(builder.toString());
                        builder = new StringBuilder().append(character);
                    } else splits.add(Character.toString(character));
                } else builder.append(character);
            }
            if (builder.length() > 0) {
                splits.add(builder.toString());
            }
            //try to combine in different manners
            //oreBasaltic MineralSand , ore BasalticMineralSand
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < splits.size(); i++) {
                buffer.append(splits.get(i));
                String tryPrefix = buffer.toString();
                if (!ORE_PREFIXES.containsKey(tryPrefix)) continue;
                prefix = tryPrefix;
                material = Joiner.on("").join(splits.subList(i + 1, splits.size())); //BasalticMineralSand
            }
            if (prefix != null && ORE_PREFIXES.containsKey(prefix)) {
                MATERIALS.put(event.getOre(), material);
                PREFIXES.put(event.getOre(), prefix);
            }
        }
    }

    public static Set<String> getOreDicts(ItemStack item) {
        return ORE_DICTS.getOrDefault(item, Collections.emptySet());
    }

    public static String getMaterial(ItemStack item) {
        return MATERIALS.get(item);
    }

    public static String getOrePrefix(ItemStack item) {
        return PREFIXES.get(item);
    }

    public static int getOrePrefixIndex(String prefix) {
        return ORE_PREFIXES.getOrDefault(prefix, Integer.MAX_VALUE);
    }

    public static String toLowerCaseUnderscore(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (i != 0 && (Character.isUpperCase(string.charAt(i)) || (
                    Character.isDigit(string.charAt(i - 1)) ^ Character.isDigit(string.charAt(i)))))
                result.append("_");
            result.append(Character.toLowerCase(string.charAt(i)));
        }
        return result.toString();
    }

    static {
        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        builder.put("ore", 0)
                .put("oreGranite", 1)
                .put("oreDiorite", 2)
                .put("oreAndesite", 3)
                .put("oreBlackgranite", 4)
                .put("oreRedgranite", 5)
                .put("oreMarble", 6)
                .put("oreBasalt", 7)
                .put("oreSand", 8)
                .put("oreRedSand", 9)
                .put("oreNetherrack", 10)
                .put("oreEndstone", 11)
                .put("crushedCentrifuged", 12)
                .put("crushedPurified", 13)
                .put("crushed", 14)
                .put("shard", 15)
                .put("clump", 16)
                .put("reduced", 17)
                .put("crystalline", 18)
                .put("cleanGravel", 19)
                .put("dirtyGravel", 20)
                .put("ingotHot", 21)
                .put("ingot", 22)
                .put("gem", 23)
                .put("gemChipped", 24)
                .put("gemFlawed", 25)
                .put("gemFlawless", 26)
                .put("gemExquisite", 27)
                .put("dustSmall", 28)
                .put("dustTiny", 29)
                .put("dustImpure", 30)
                .put("dustPure", 31)
                .put("dust", 32)
                .put("nugget", 33)
                .put("plateDense", 34)
                .put("plateDouble", 35)
                .put("plate", 36)
                .put("round", 37)
                .put("foil", 38)
                .put("stickLong", 39)
                .put("stick", 40)
                .put("bolt", 41)
                .put("screw", 42)
                .put("ring", 43)
                .put("springSmall", 44)
                .put("spring", 45)
                .put("wireFine", 46)
                .put("rotor", 47)
                .put("gearSmall", 48)
                .put("gear", 49)
                .put("lens", 50)
                .put("toolHeadSword", 51)
                .put("toolHeadPickaxe", 52)
                .put("toolHeadShovel", 53)
                .put("toolHeadAxe", 54)
                .put("toolHeadHoe", 55)
                .put("toolHeadSense", 56)
                .put("toolHeadFile", 57)
                .put("toolHeadHammer", 58)
                .put("toolHeadSaw", 59)
                .put("toolHeadBuzzSaw", 60)
                .put("toolHeadScrewdriver", 61)
                .put("toolHeadDrill", 62)
                .put("toolHeadChainsaw", 63)
                .put("toolHeadWrench", 64)
                .put("turbineBlade", 65)
                .put("paneGlass", 66)
                .put("blockGlass", 67)
                .put("block", 68)
                .put("log", 69)
                .put("plank", 70)
                .put("stone", 71)
                .put("frameGt", 72)
                .put("pipeTinyFluid", 73)
                .put("pipeSmallFluid", 74)
                .put("pipeNormalFluid", 75)
                .put("pipeLargeFluid", 76)
                .put("pipeHugeFluid", 77)
                .put("pipeQuadrupleFluid", 78)
                .put("pipeNonupleFluid", 79)
                .put("pipeTinyItem", 80)
                .put("pipeSmallItem", 81)
                .put("pipeNormalItem", 82)
                .put("pipeLargeItem", 83)
                .put("pipeHugeItem", 84)
                .put("pipeSmallRestrictive", 85)
                .put("pipeNormalRestrictive", 86)
                .put("pipeLargeRestrictive", 87)
                .put("pipeHugeRestrictive", 88)
                .put("wireGtHex", 89)
                .put("wireGtOctal", 90)
                .put("wireGtQuadruple", 91)
                .put("wireGtDouble", 92)
                .put("wireGtSingle", 93)
                .put("cableGtHex", 94)
                .put("cableGtOctal", 95)
                .put("cableGtQuadruple", 96)
                .put("cableGtDouble", 97)
                .put("cableGtSingle", 98)
                .put("craftingLens", 99)
                .put("dye", 100)
                .put("battery", 101)
                .put("circuit", 102)
                .put("component", 103);
        ORE_PREFIXES = builder.build();
    }
}
