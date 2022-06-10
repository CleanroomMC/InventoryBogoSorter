package com.cleanroommc.invtweaks;

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

    @SubscribeEvent
    public static void onItemRegistration(OreDictionary.OreRegisterEvent event) {
        ORE_DICTS.computeIfAbsent(event.getOre(), key -> new HashSet<>()).add(event.getName());
    }

    public static Set<String> getOreDicts(ItemStack item) {
        return ORE_DICTS.getOrDefault(item, Collections.emptySet());
    }
}
