package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.common.OreDictHelper;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.config.BogoSortCommandTree;
import com.cleanroommc.bogosorter.common.config.PlayerConfig;
import com.cleanroommc.bogosorter.common.config.Serializer;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.NetworkUtils;
import com.cleanroommc.bogosorter.common.sort.DefaultRules;
import com.cleanroommc.bogosorter.compat.DefaultCompat;
import com.cleanroommc.modularui.api.KeyBindAPI;
import gregtech.GregTechVersion;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = BogoSorter.ID, name = BogoSorter.NAME, version = BogoSorter.VERSION, dependencies = "required-after:modularui@[1.0.6,);required-after:mixinbooter@[4.2,)")
@Mod.EventBusSubscriber(modid = BogoSorter.ID)
public class BogoSorter {

    public static final String ID = "bogosorter";
    public static final String NAME = "Inventory Bogo Sorter";
    public static final String VERSION = "1.0.6";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    private static boolean anyGtLoaded = false;
    private static boolean tconstructLoaded = false;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        anyGtLoaded = Loader.isModLoaded("gregtech");
        tconstructLoaded = Loader.isModLoaded("tconstruct");
        NetworkHandler.init();
        OreDictHelper.init();
        DefaultRules.init(BogoSortAPI.INSTANCE);
        DefaultCompat.init(BogoSortAPI.INSTANCE);
        if (NetworkUtils.isDedicatedClient()) {
            Serializer.loadConfig();
            MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
            PlayerConfig.syncToServer();
        }
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        if (NetworkUtils.isDedicatedClient()) {
            ClientRegistry.registerKeyBinding(ClientEventHandler.configGuiKey);
            ClientRegistry.registerKeyBinding(ClientEventHandler.sortKey);
            KeyBindAPI.forceCheckKeyBind(ClientEventHandler.configGuiKey);
            KeyBindAPI.forceCheckKeyBind(ClientEventHandler.sortKey);
            KeyBindAPI.setCompatible(ClientEventHandler.sortKey, Minecraft.getMinecraft().gameSettings.keyBindPickBlock);
        }
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new BogoSortCommandTree());
    }

    public static boolean isAnyGtLoaded() {
        return anyGtLoaded;
    }

    @SuppressWarnings("all")
    public static boolean isGTCELoaded() {
        return anyGtLoaded && GregTechVersion.MAJOR == 1;
    }

    @SuppressWarnings("all")
    public static boolean isGTCEuLoaded() {
        return anyGtLoaded && GregTechVersion.MAJOR >= 2;
    }

    public static boolean isTConstructLoaded() {
        return tconstructLoaded;
    }
}
