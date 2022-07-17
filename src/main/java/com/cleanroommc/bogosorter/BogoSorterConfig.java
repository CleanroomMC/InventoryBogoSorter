package com.cleanroommc.bogosorter;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid=BogoSorter.ID, name=BogoSorter.NAME)
public class BogoSorterConfig
{
    @Config.Name("Enable Auto Refill")
    public static boolean enableAutoRefill = true;

    @Config.RangeInt(min = 0, max = Short.MAX_VALUE)
    @Config.Name("Auto Refill Damage Threshold")
    @Config.Comment({"Damage threshold of when to trigger the auto refill.",
            "Example: 5 = Trigger when an item has 5 uses left"})
    public static int autoRefillDamageThreshold = 1;


    @Mod.EventBusSubscriber(modid = BogoSorter.ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(BogoSorter.ID)) {
                ConfigManager.sync(BogoSorter.ID, Config.Type.INSTANCE);
            }
        }
    }
}
