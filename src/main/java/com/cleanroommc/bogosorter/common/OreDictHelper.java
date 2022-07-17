package com.cleanroommc.bogosorter.common;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

@Mod.EventBusSubscriber(modid = BogoSorter.ID)
public class OreDictHelper {

    private static final Map<ItemStack, Set<String>> ORE_DICTS = new Object2ObjectOpenCustomHashMap<>(BogoSortAPI.ITEM_META_HASH_STRATEGY);
    private static final Map<ItemStack, String> MATERIALS = new Object2ObjectOpenCustomHashMap<>(BogoSortAPI.ITEM_META_HASH_STRATEGY);
    private static final Map<ItemStack, String> PREFIXES = new Object2ObjectOpenCustomHashMap<>(BogoSortAPI.ITEM_META_HASH_STRATEGY);
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

        String[] defaultOrePrefixOrderGt = {
                "ingot", "ingotHot", "gemChipped", "gemFlawed", "gem", "gemFlawless", "gemExquisite", "dustTiny", "dustSmall", "dust", "nugget", "block",
                "plate", "plateDouble", "plateDense", "gear", "bolt", "stick", "stickLong", "ring", "screw", "round", "foil", "wireFine", "springSmall", "spring",
                "turbineBlade", "rotor", "lens", "dustImpure", "dustPure", "crushed", "crushedCentrifuged", "crushedPurified", "shard", "clump", "reduced",
                "crystalline", "dirtyGravel", "cleanGravel", "toolHeadSword", "toolHeadPickaxe", "toolHeadShovel", "toolHeadAxe", "toolHeadHoe", "toolHeadSense",
                "toolHeadFile", "toolHeadHammer", "toolHeadSaw", "toolHeadBuzzSaw", "toolHeadScrewdriver", "toolHeadDrill", "toolHeadChainsaw", "toolHeadWrench",
                "pipeTinyFluid", "pipeSmallFluid", "pipeNormalFluid", "pipeLargeFluid", "pipeHugeFluid", "pipeQuadrupleFluid", "pipeNonupleFluid", "pipeTinyItem",
                "pipeSmallItem", "pipeNormalItem", "pipeLargeItem", "pipeHugeItem", "pipeSmallRestrictive", "pipeNormalRestrictive", "pipeLargeRestrictive",
                "pipeHugeRestrictive", "wireGtSingle", "wireGtDouble", "wireGtQuadruple", "wireGtOctal", "wireGtHex", "cableGtSingle", "cableGtDouble",
                "cableGtQuadruple", "cableGtOctal", "cableGtHex", "frameGt", "ore", "oreGranite", "oreDiorite", "oreAndesite", "oreBlackgranite", "oreRedgranite",
                "oreMarble", "oreBasalt", "oreSand", "oreRedSand", "oreNetherrack", "oreEndstone"
        };

        String[] defaultOrePrefixOrder = {"ingot", "gem", "dust", "nugget", "block", "plate", "gear", "stick", "ore", "log"};


        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        String[] prefixes = BogoSorter.isAnyGtLoaded()? defaultOrePrefixOrderGt : defaultOrePrefixOrder;
        int i = 0;
        for (String orePrefix : prefixes) {
            builder.put(orePrefix, i++);
        }
        ORE_PREFIXES = builder.build();
    }
}
