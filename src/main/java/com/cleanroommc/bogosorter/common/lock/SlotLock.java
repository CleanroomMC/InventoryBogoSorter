package com.cleanroommc.bogosorter.common.lock;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.bogosorter.common.Align;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.UpdateSlotLock;
import com.cleanroommc.modularui.drawable.ColorType;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class SlotLock {

    private static final DataSerializer<Long> LONG_SERIALIZER = new DataSerializer<>() {

        @Override
        public void write(PacketBuffer buf, Long value) {
            buf.writeLong(value);
        }

        @Override
        public Long read(PacketBuffer buf) throws IOException {
            return buf.readLong();
        }

        @Override
        public DataParameter<Long> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        public Long copyValue(Long value) {
            return value;
        }
    };
    private static DataParameter<Long> dp = EntityDataManager.createKey(EntityPlayer.class, LONG_SERIALIZER);

    private static LockSlotCapability clientCap;

    public static LockSlotCapability getClientCap() {
        if (clientCap == null) {
            if (Minecraft.getMinecraft().player == null) return LockSlotCapability.UNLOCKED;
            clientCap = LockSlotCapability.getForPlayer(Minecraft.getMinecraft().player);
        }
        return clientCap;
    }

    public static void sendChangesToServer() {
        NetworkHandler.sendToServer(new UpdateSlotLock(getClientCap().getLockedSlots()));
    }

    public static final long HOTBAR_SLOTS = 0b111111111;
    public static final long HOTBAR_AND_OFFHAND_SLOTS = HOTBAR_SLOTS | (1L << 40);

    public static Align.Corner alignment = Align.Corner.TOP_LEFT;
    public static int iconOffsetX = -1;
    public static int iconOffsetY = -1;
    public static int iconWidth = 7;
    public static int iconHeight = 9;
    public static float iconScale = 0.5f;
    public static int iconColor = Color.BLUE.main;

    private static final ColorType lockColor = new ColorType("bogo_lock_color", w -> iconColor);
    private static final UITexture lockTexture = UITexture.fullImage(BogoSorter.ID, "gui/lock", lockColor);

    public static void drawLock(int slotX, int slotY, int slotW, int slotH) {
        int iw = (int) (iconWidth * iconScale);
        int ih = (int) (iconHeight * iconScale);
        int x = alignment.x.apply(slotX, slotW, iw, iconOffsetX);
        int y = alignment.y.apply(slotY, slotH, ih, iconOffsetY);
        lockTexture.draw(GuiContext.getDefault(), x, y, iw, ih, WidgetTheme.getDefault().getTheme());
    }

    public static void drawHotbarOverlay() {
        LockSlotCapability cap = getClientCap();
        if ((cap.getLockedSlots() & HOTBAR_AND_OFFHAND_SLOTS) == 0) return;
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        if ((cap.getLockedSlots() & HOTBAR_SLOTS) != 0) {
            for (int i = 0; i < 9; i++) {
                if (!cap.isSlotLocked(i)) {
                    continue;
                }

                int x = (res.getScaledWidth() / 2 - 87) + (i * 20), y = res.getScaledHeight() - 18;
                drawLock(x, y, 18, 18);
            }
        }

        if (cap.isSlotLocked(40) && !Minecraft.getMinecraft().player.inventory.offHandInventory.get(0).isEmpty()) {
            drawLock((res.getScaledWidth() / 2 - 116), (res.getScaledHeight() - 18), 18, 18);
        }
        GlStateManager.enableDepth();
    }

    public static boolean onGuiKeyInput(GuiContainer gui) {
        Slot slot = gui.getSlotUnderMouse();
        if (slot == null || !BogoSortAPI.isPlayerSlot(slot)) return false;
        if (GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindDrop)) {
            return getClientCap().isSlotLocked(slot.getSlotIndex());
        } else if (GameSettings.isKeyDown(ClientEventHandler.keyLockSlot)) {
            if (slot.getHasStack()) {
                getClientCap().toggleLock(slot.getSlotIndex());
                sendChangesToServer();
                return true;
            }
            if (getClientCap().isSlotLocked(slot.getSlotIndex())) {
                getClientCap().setSlotLocked(slot.getSlotIndex(), false);
                sendChangesToServer();
                return true;
            }
        }
        // handle cancellation of swapping with hotbar slot
        int[] zeroToNine = {Keyboard.KEY_1, Keyboard.KEY_2, Keyboard.KEY_3, Keyboard.KEY_4, Keyboard.KEY_5, Keyboard.KEY_6, Keyboard.KEY_7, Keyboard.KEY_8, Keyboard.KEY_9};
        for (int i = 0; i < 9; i++) {
            if (Keyboard.isKeyDown(zeroToNine[i])) {
                if (getClientCap().isSlotLocked(i) || getClientCap().isSlotLocked(slot.getSlotIndex())) return true;
            }
        }
        return false;
    }

    public static boolean onGuiMouseInput(GuiContainer gui) {
        if (Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1) {
            Slot slot = gui.getSlotUnderMouse();
            return slot != null && BogoSortAPI.isPlayerSlot(slot) && getClientCap().isSlotLocked(slot.getSlotIndex());
        }
        return false;
    }
}
