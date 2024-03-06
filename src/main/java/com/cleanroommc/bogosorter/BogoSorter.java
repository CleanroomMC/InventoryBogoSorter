package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.common.HotbarSwap;
import com.cleanroommc.bogosorter.common.OreDictHelper;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.XSTR;
import com.cleanroommc.bogosorter.common.config.BogoSortCommandTree;
import com.cleanroommc.bogosorter.common.config.PlayerConfig;
import com.cleanroommc.bogosorter.common.config.Serializer;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.NetworkUtils;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import com.cleanroommc.bogosorter.common.sort.ButtonHandler;
import com.cleanroommc.bogosorter.common.sort.DefaultRules;
import com.cleanroommc.bogosorter.compat.DefaultCompat;
import com.cleanroommc.modularui.keybind.KeyBindAPI;
import gregtech.GregTechVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.Month;

@Mod(modid = BogoSorter.ID,
        name = BogoSorter.NAME,
        version = BogoSorter.VERSION,
        dependencies =
                "required-after:modularui@[2.4.2,3.0.0);" +
                "required-after:mixinbooter@[8.0,)")
@Mod.EventBusSubscriber(modid = BogoSorter.ID)
public class BogoSorter {

    public static final String ID = Tags.MODID;
    public static final String NAME = "Inventory Bogo Sorter";
    public static final String VERSION = Tags.VERSION;

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final XSTR RND = new XSTR();

    private static boolean anyGtLoaded = false;
    private static boolean tconstructLoaded = false;
    private static boolean anyIc2Loaded = false;
    private static boolean ic2ClassicLoaded = false;
    private static boolean quarkLoaded = false;
    private static boolean ae2Loaded = false;
    private static boolean expandableInventoryLoaded = false;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        anyGtLoaded = Loader.isModLoaded("gregtech");
        tconstructLoaded = Loader.isModLoaded("tconstruct");
        anyIc2Loaded = Loader.isModLoaded("ic2");
        quarkLoaded = Loader.isModLoaded("quark");
        ae2Loaded = Loader.isModLoaded("appliedenergistics2");
        expandableInventoryLoaded = Loader.isModLoaded("expandableinventory");
        if (anyIc2Loaded) {
            ModContainer container = Loader.instance().getIndexedModList().get("ic2");
            ic2ClassicLoaded = container.getName().endsWith("Classic");
        }
        NetworkHandler.init();
        OreDictHelper.init();
        BogoSortAPI.INSTANCE.remapSortRule("is_block", "block_type");
        DefaultRules.init(BogoSortAPI.INSTANCE);
        DefaultCompat.init(BogoSortAPI.INSTANCE);
        Serializer.loadConfig();
        MinecraftForge.EVENT_BUS.register(RefillHandler.class);
        if (NetworkUtils.isDedicatedClient()) {
            MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
            MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
            MinecraftForge.EVENT_BUS.register(ButtonHandler.class);
            MinecraftForge.EVENT_BUS.register(HotbarSwap.class);
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

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote && event.getEntity() instanceof EntityPlayer) {
            PlayerConfig.syncToServer();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.world.getTotalWorldTime() % 100 == 0) {
            PlayerConfig.checkPlayers();
        }
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

    public static boolean isAnyIc2Loaded() {
        return anyIc2Loaded;
    }

    public static boolean isIc2ClassicLoaded() {
        return anyIc2Loaded && ic2ClassicLoaded;
    }

    public static boolean isIc2ExpLoaded() {
        return anyIc2Loaded && !ic2ClassicLoaded;
    }

    public static boolean isQuarkLoaded() {
        return quarkLoaded;
    }

    public static boolean isAe2Loaded() {
        return ae2Loaded;
    }

    public static boolean isExpandableInventoryLoaded() {
        return expandableInventoryLoaded;
    }

    public static boolean isAprilFools() {
        LocalDate date = LocalDate.now();
        return date.getMonth() == Month.APRIL && date.getDayOfMonth() == 1;
    }
}
