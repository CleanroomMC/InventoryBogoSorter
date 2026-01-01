package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.common.HotbarSwap;
import com.cleanroommc.bogosorter.common.OreDictHelper;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.XSTR;
import com.cleanroommc.bogosorter.common.config.BogoSortCommandTree;
import com.cleanroommc.bogosorter.common.config.PlayerConfig;
import com.cleanroommc.bogosorter.common.config.Serializer;
import com.cleanroommc.bogosorter.common.lock.LockSlotCapability;
import com.cleanroommc.bogosorter.common.lock.SlotLock;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.NetworkUtils;
import com.cleanroommc.bogosorter.common.network.UpdateSlotLock;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import com.cleanroommc.bogosorter.common.sort.ButtonHandler;
import com.cleanroommc.bogosorter.common.sort.DefaultRules;
import com.cleanroommc.bogosorter.compat.DefaultCompat;
import com.cleanroommc.bogosorter.compat.ModularScreenOverlay;
import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.keybind.KeyBindAPI;
import com.cleanroommc.modularui.overlay.OverlayHandler;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import gregtech.GregTechVersion;
import org.apache.commons.lang3.ArrayUtils;
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
                "required-after-client:modularui@[3.0.6,4.0.0);" +
                        "required-after-client:key_binding_patch@[1.3.3.3,);" +
                        "required-after:mixinbooter@[8.0,)")
@Mod.EventBusSubscriber(modid = BogoSorter.ID)
public class BogoSorter {

    public static final String ID = Tags.MODID;
    public static final String NAME = "Inventory Bogo Sorter";
    public static final String VERSION = Tags.VERSION;

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final XSTR RND = new XSTR();

    @CapabilityInject(LockSlotCapability.class)
    public static Capability<LockSlotCapability> favoriteSlotCap = null;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        NetworkHandler.init();
        OreDictHelper.init();
        BogoSortAPI.INSTANCE.remapSortRule("is_block", "block_type");
        DefaultRules.init(BogoSortAPI.INSTANCE);
        DefaultCompat.init(BogoSortAPI.INSTANCE);
        Serializer.loadConfig();
        MinecraftForge.EVENT_BUS.register(RefillHandler.class);
        CapabilityManager.INSTANCE.register(LockSlotCapability.class, new LockSlotCapability.Storage(), LockSlotCapability.Default::new);
        if (NetworkUtils.isDedicatedClient()) {
            MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
            MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
            MinecraftForge.EVENT_BUS.register(ButtonHandler.class);
            MinecraftForge.EVENT_BUS.register(HotbarSwap.class);

            ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new SlotLock());

            // hacky way to fix shortcuts on mui screens
            // mui intercepts inputs way earlier than bogo
            // so we just add an invisible panel over the whole screen that just acts as a giant button
            // TODO proper fix on mui side?
            OverlayManager.register(new OverlayHandler(g -> g instanceof IMuiScreen, g -> new ModularScreen(new ModularScreenOverlay(g.getClass().getSimpleName() + "_bogo_overlay"))));
        }
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        if (NetworkUtils.isDedicatedClient()) {
            ClientRegistry.registerKeyBinding(ClientEventHandler.configGuiKey);
            ClientRegistry.registerKeyBinding(ClientEventHandler.sortKey);
            ClientRegistry.registerKeyBinding(ClientEventHandler.keyLockSlot);

            GameSettings s = Minecraft.getMinecraft().gameSettings;
            s.keyBindDrop = replaceKeybind(s.keyBindDrop, ClientEventHandler.keyDropReplacement);
            s.keyBindSwapHands = replaceKeybind(s.keyBindSwapHands, ClientEventHandler.keySwapHandReplacement);

            KeyBindAPI.forceCheckKeyBind(ClientEventHandler.configGuiKey);
            KeyBindAPI.forceCheckKeyBind(ClientEventHandler.sortKey);
            KeyBindAPI.forceCheckKeyBind(ClientEventHandler.keyLockSlot);
            KeyBindAPI.forceCheckKeyBind(ClientEventHandler.moveAll.getKeyBinding());
            KeyBindAPI.forceCheckKeyBind(ClientEventHandler.moveAllSame.getKeyBinding());
            KeyBindAPI.forceCheckKeyBind(ClientEventHandler.moveSingle.getKeyBinding());
            KeyBindAPI.forceCheckKeyBind(ClientEventHandler.moveSingleEmpty.getKeyBinding());
            KeyBindAPI.forceCheckKeyBind(ClientEventHandler.throwAll.getKeyBinding());
            KeyBindAPI.forceCheckKeyBind(ClientEventHandler.throwAllSame.getKeyBinding());
            KeyBindAPI.setCompatible(ClientEventHandler.sortKey, Minecraft.getMinecraft().gameSettings.keyBindPickBlock);
        }
    }

    private static KeyBinding replaceKeybind(KeyBinding old, KeyBinding newKey) {
        GameSettings s = Minecraft.getMinecraft().gameSettings;
        // replace mc drop keybind with custom version
        int i = ArrayUtils.indexOf(s.keyBindings, old);
        if (i < 0) {
            s.keyBindings = ArrayUtils.add(s.keyBindings, newKey);
        } else {
            s.keyBindings[i] = ClientEventHandler.keyDropReplacement;
        }
        ClientEventHandler.keyDropReplacement.setKeyModifierAndCode(old.getKeyModifier(), old.getKeyCode());
        return newKey;
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
