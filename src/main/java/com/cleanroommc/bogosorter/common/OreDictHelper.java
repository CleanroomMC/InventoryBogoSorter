package com.cleanroommc.bogosorter.common;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import gregtech.api.items.toolitem.ToolMetaItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.tinkering.IMaterialItem;

import java.util.*;

@Mod.EventBusSubscriber(modid = BogoSorter.ID)
public class OreDictHelper {

    private static final Map<ItemStack, Set<String>> ORE_DICTS = new Object2ObjectOpenCustomHashMap<>(BogoSortAPI.ITEM_META_HASH_STRATEGY);
    private static final Map<ItemStack, String> MATERIALS = new Object2ObjectOpenCustomHashMap<>(BogoSortAPI.ITEM_META_HASH_STRATEGY);
    private static final Map<ItemStack, String> PREFIXES = new Object2ObjectOpenCustomHashMap<>(BogoSortAPI.ITEM_META_HASH_STRATEGY);
    private static final Map<String, Integer> ORE_PREFIXES = new HashMap<>();
    private static final List<String> ORE_PREFIXES_LIST = new ArrayList<>();
    private static final Map<String, String[]> orePrefixOwnerMap = new Object2ObjectOpenHashMap<>();

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
        if (BogoSorter.isAnyGtLoaded() && item.getItem() instanceof ToolMetaItem) {
            return getGtToolMaterial(item);
        }
        if (BogoSorter.isTConstructLoaded() && item.getItem() instanceof IMaterialItem) {
            return ((IMaterialItem) item.getItem()).getMaterialID(item);
        }
        return MATERIALS.get(item);
    }

    @Optional.Method(modid = "gregtech")
    @NotNull
    public static String getGtToolMaterial(ItemStack itemStack) {
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

    public static List<String> getOrePrefixesList() {
        return ORE_PREFIXES_LIST;
    }

    private static void addOrePrefix(String[] owner, String... orePrefixes) {
        for (String prefix : orePrefixes) {
            if (orePrefixOwnerMap.containsKey(prefix)) {
                orePrefixOwnerMap.put(prefix, ArrayUtils.addAll(orePrefixOwnerMap.get(prefix), owner));
            } else {
                orePrefixOwnerMap.put(prefix, owner);
            }
        }
    }

    private static void addOrePrefix(String owner, String... orePrefixes) {
        addOrePrefix(new String[]{owner}, orePrefixes);
    }

    private static boolean isPrefixLoaded(String prefix) {
        String[] owners = orePrefixOwnerMap.get(prefix);
        if (owners == null || owners.length == 0) return true;
        for (String owner : owners) {
            if (Loader.isModLoaded(owner)) {
                return true;
            }
        }
        return false;
    }

    public static void loadFromJson(JsonArray json) {
        ORE_PREFIXES.clear();
        ORE_PREFIXES_LIST.clear();
        int i = 0;
        for (JsonElement jsonElement : json) {
            if (!jsonElement.isJsonPrimitive() && jsonElement.isJsonNull()) {
                String orePrefix = jsonElement.getAsString();
                ORE_PREFIXES.put(orePrefix, i++);
                ORE_PREFIXES_LIST.add(orePrefix);
            }
        }
    }

    public static void init() {
        addOrePrefix("gregtech", "ingotHot", "gemChipped", "gemFlawed", "gemFlawless", "gemExquisite", "dustTiny", "dustSmall",
                "plateDouble", "plateDense", "gear", "bolt", "stick", "stickLong", "ring", "screw", "round", "foil", "wireFine", "springSmall", "spring",
                "turbineBlade", "rotor", "lens", "dustImpure", "dustPure", "crushed", "crushedCentrifuged", "crushedPurified", "shard", "clump", "reduced",
                "crystalline", "dirtyGravel", "cleanGravel", "toolHeadSword", "toolHeadPickaxe", "toolHeadShovel", "toolHeadAxe", "toolHeadHoe", "toolHeadSense",
                "toolHeadFile", "toolHeadHammer", "toolHeadSaw", "toolHeadBuzzSaw", "toolHeadScrewdriver", "toolHeadDrill", "toolHeadChainsaw", "toolHeadWrench",
                "pipeTinyFluid", "pipeSmallFluid", "pipeNormalFluid", "pipeLargeFluid", "pipeHugeFluid", "pipeQuadrupleFluid", "pipeNonupleFluid", "pipeTinyItem",
                "pipeSmallItem", "pipeNormalItem", "pipeLargeItem", "pipeHugeItem", "pipeSmallRestrictive", "pipeNormalRestrictive", "pipeLargeRestrictive",
                "pipeHugeRestrictive", "wireGtSingle", "wireGtDouble", "wireGtQuadruple", "wireGtOctal", "wireGtHex", "cableGtSingle", "cableGtDouble",
                "cableGtQuadruple", "cableGtOctal", "cableGtHex", "frameGt", "oreGranite", "oreDiorite", "oreAndesite", "oreBlackgranite", "oreRedgranite",
                "oreMarble", "oreBasalt", "oreSand", "oreRedSand", "oreNetherrack", "oreEndstone");
        addOrePrefix("thermalfoundation", "gear", "stick", "plate", "fuel", "crystal", "rod", "coin");
        addOrePrefix("thaumcraft", "cluster", "oreCrystal");
        addOrePrefix("ic2", "plate", "plateDense", "crushed", "crushedPurified");
        addOrePrefix("immersiveengineering", "plate", "wire", "blockSheetmetal");
        addOrePrefix("enderio", "ball");

        String[] defaultOrePrefixOrder = {
                "ingot", "ingotHot", "gemChipped", "gemFlawed", "gem", "gemFlawless", "gemExquisite", "dustTiny", "dustSmall", "dust", "nugget", "block", "blockSheetmetal",
                "plate", "plateDouble", "plateDense", "gear", "bolt", "stick", "stickLong", "ring", "screw", "round", "foil", "wireFine", "wire", "springSmall", "spring",
                "ball", "crystal", "coin", "fuel",
                "turbineBlade", "rotor", "lens", "dustImpure", "dustPure", "crushed", "crushedCentrifuged", "crushedPurified", "shard", "clump", "reduced",
                "crystalline", "dirtyGravel", "cleanGravel", "toolHeadSword", "toolHeadPickaxe", "toolHeadShovel", "toolHeadAxe", "toolHeadHoe", "toolHeadSense",
                "toolHeadFile", "toolHeadHammer", "toolHeadSaw", "toolHeadBuzzSaw", "toolHeadScrewdriver", "toolHeadDrill", "toolHeadChainsaw", "toolHeadWrench",
                "pipeTinyFluid", "pipeSmallFluid", "pipeNormalFluid", "pipeLargeFluid", "pipeHugeFluid", "pipeQuadrupleFluid", "pipeNonupleFluid", "pipeTinyItem",
                "pipeSmallItem", "pipeNormalItem", "pipeLargeItem", "pipeHugeItem", "pipeSmallRestrictive", "pipeNormalRestrictive", "pipeLargeRestrictive",
                "pipeHugeRestrictive", "wireGtSingle", "wireGtDouble", "wireGtQuadruple", "wireGtOctal", "wireGtHex", "cableGtSingle", "cableGtDouble",
                "cableGtQuadruple", "cableGtOctal", "cableGtHex", "frameGt", "glass", "ore", "oreGranite", "oreDiorite", "oreAndesite", "oreBlackgranite", "oreRedgranite",
                "oreMarble", "oreBasalt", "oreSand", "oreRedSand", "oreNetherrack", "oreEndstone", "oreCrystal", "log", "rod"
        };

        ORE_PREFIXES.clear();
        ORE_PREFIXES_LIST.clear();
        int i = 0;
        for (String orePrefix : defaultOrePrefixOrder) {
            if (isPrefixLoaded(orePrefix)) {
                ORE_PREFIXES.put(orePrefix, i++);
                ORE_PREFIXES_LIST.add(orePrefix);
            }
        }
    }
}
