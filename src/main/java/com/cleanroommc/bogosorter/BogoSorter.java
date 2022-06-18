package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.common.config.BogoSortCommandTree;
import com.cleanroommc.bogosorter.common.config.ConfigReloadCommand;
import com.cleanroommc.bogosorter.common.config.Serializer;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.sort.DefaultRules;
import com.cleanroommc.bogosorter.compat.DefaultCompat;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = BogoSorter.ID, name = BogoSorter.NAME, version = BogoSorter.VERSION)
@Mod.EventBusSubscriber(modid = BogoSorter.ID)
public class BogoSorter {

    public static final String ID = "bogosorter";
    public static final String NAME = "Inventory Bogo Sorter";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final Serializer SERIALIZER = new Serializer();

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        NetworkHandler.init();
        DefaultRules.init(BogoSortAPI.INSTANCE);
        DefaultCompat.init(BogoSortAPI.INSTANCE);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        SERIALIZER.loadConfig();
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new BogoSortCommandTree());
    }
}
