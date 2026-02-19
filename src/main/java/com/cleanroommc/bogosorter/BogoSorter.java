package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.common.XSTR;
import com.cleanroommc.bogosorter.common.config.BogoSortCommandTree;
import com.cleanroommc.bogosorter.common.config.PlayerConfig;
import com.cleanroommc.bogosorter.common.lock.LockSlotCapability;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.UpdateSlotLock;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import gregtech.GregTechVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.Month;
import java.util.function.Predicate;

@Mod(modid = BogoSorter.ID,
        name = BogoSorter.NAME,
        version = BogoSorter.VERSION,
        dependencies =
                "required-after-client:modularui@[3.1.3,4.0.0);" +
                        "required-after-client:key_binding_patch@[1.3.3.3,);")
@Mod.EventBusSubscriber(modid = BogoSorter.ID)
public class BogoSorter {

    public static final String ID = Tags.MODID;
    public static final String NAME = "Inventory Bogo Sorter";
    public static final String VERSION = Tags.VERSION;

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final XSTR RND = new XSTR();

    @SidedProxy(
            modId = ID,
            clientSide = "com.cleanroommc.bogosorter.ClientProxy",
            serverSide = "com.cleanroommc.bogosorter.CommonProxy")
    public static CommonProxy proxy;

    @CapabilityInject(LockSlotCapability.class)
    public static Capability<LockSlotCapability> favoriteSlotCap = null;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        proxy.preInit();
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.postInit();
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

    @SubscribeEvent
    public static void onAttachCap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(LockSlotCapability.ID, new LockSlotCapability.Provider());
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP player) {
            NetworkHandler.sendToPlayer(new UpdateSlotLock(LockSlotCapability.getForPlayer(player).getLockedSlots()), player);
        }
    }

    public static boolean isAprilFools() {
        LocalDate date = LocalDate.now();
        return date.getMonth() == Month.APRIL && date.getDayOfMonth() == 1;
    }

    public enum Mods {

        AE2(ModIds.AE2),
        EXPANDABLE_INVENTORY(ModIds.EXPANDABLE_INVENTORY),
        GT_ANY(ModIds.GREGTECH),

        @SuppressWarnings("ConstantValue")
        GTCE(ModIds.GREGTECH, m -> GregTechVersion.MAJOR == 1),

        @SuppressWarnings("ConstantValue")
        GTCEu(ModIds.GREGTECH, m -> GregTechVersion.MAJOR >= 2),

        IC2_ANY(ModIds.IC2),
        IC2_CLASSIC(ModIds.IC2, m -> m.getName().endsWith("Classic")),
        IC2_EXP(ModIds.IC2, m -> !m.getName().endsWith("Classic")),
        ITEM_FAVORITES(ModIds.ITEM_FAVORITES),
        QUARK(ModIds.QUARK),
        T_CONSTRUCT(ModIds.T_CONSTRUCT);

        public final String id;
        private boolean loaded = false;
        private boolean initialized = false;
        private final Predicate<ModContainer> extraLoadedCheck;

        Mods(String id) {
            this(id, null);
        }

        Mods(String id, @Nullable Predicate<ModContainer> extraLoadedCheck) {
            this.id = id;
            this.extraLoadedCheck = extraLoadedCheck;
        }

        public boolean isLoaded() {
            if (!this.initialized) {
                this.loaded = Loader.isModLoaded(this.id);
                if (this.loaded && this.extraLoadedCheck != null) {
                    this.loaded = this.extraLoadedCheck.test(Loader.instance().getIndexedModList().get(this.id));
                }
                this.initialized = true;
            }
            return this.loaded;
        }
    }

    public static class ModIds {

        public static final String AE2 = "appliedenergistics2";
        public static final String EXPANDABLE_INVENTORY = "expandableinventory";
        public static final String GREGTECH = "gregtech";
        public static final String IC2 = "ic2";
        public static final String ITEM_FAVORITES = "itemfav";
        public static final String QUARK = "quark";
        public static final String T_CONSTRUCT = "tconstruct";
    }
}
