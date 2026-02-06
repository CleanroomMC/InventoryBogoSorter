package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.common.HotbarSwap;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.lock.SlotLock;
import com.cleanroommc.bogosorter.common.sort.ButtonHandler;
import com.cleanroommc.bogosorter.compat.ModularScreenOverlay;
import com.cleanroommc.modularui.keybind.KeyBindAPI;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.OpenScreenEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.ArrayUtils;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        MinecraftForge.EVENT_BUS.register(ButtonHandler.class);
        MinecraftForge.EVENT_BUS.register(HotbarSwap.class);

        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new SlotLock());
    }

    @SubscribeEvent
    public void onOpenScreen(OpenScreenEvent event) {
        // hacky way to fix shortcuts on mui screens
        // mui intercepts inputs way earlier than bogo
        // so we just add an invisible panel over the whole screen that just acts as a giant button
        if (event.isModularScreen()) {
            event.addOverlay(new ModularScreen(BogoSorter.ID, new ModularScreenOverlay("bogosorter_overlay")));
        }
    }

    @Override
    public void postInit() {
        super.postInit();
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
}
