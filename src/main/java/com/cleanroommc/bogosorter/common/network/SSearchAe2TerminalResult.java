package com.cleanroommc.bogosorter.common.network;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.ForgeDirection;

import com.cleanroommc.bogosorter.common.dropoff.render.RendererCube;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCubeTarget;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SSearchAe2TerminalResult implements IPacket {

    private static final int LEGACY_TARGET_PACKET_BYTES = Integer.BYTES * 4;
    private static final int SINGLE_TARGET_CAPACITY = 1;
    private static final int MAX_RENDER_TARGETS = 256;
    private static final Color AE2_HIGHLIGHT = Color.CYAN;
    private static final double BLOCK_CENTER_OFFSET = 0.5D;
    private static final double FACE_OFFSET = 0.35D;
    private static final double YAW_OFFSET_DEGREES = 90.0D;

    private List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
    private boolean shouldLookAtTerminal;
    private int x;
    private int y;
    private int z;
    private int sideOrdinal;

    public SSearchAe2TerminalResult() {}

    public SSearchAe2TerminalResult(List<RendererCubeTarget> rendererCubeTargets, boolean shouldLookAtTerminal, int x,
        int y, int z, int sideOrdinal) {
        this.rendererCubeTargets = rendererCubeTargets;
        this.shouldLookAtTerminal = shouldLookAtTerminal;
        this.x = x;
        this.y = y;
        this.z = z;
        this.sideOrdinal = sideOrdinal;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(this.rendererCubeTargets.size());
        for (RendererCubeTarget target : this.rendererCubeTargets) {
            buf.writeInt(
                target.getBlockPos()
                    .getX());
            buf.writeInt(
                target.getBlockPos()
                    .getY());
            buf.writeInt(
                target.getBlockPos()
                    .getZ());
            buf.writeInt(
                target.getColor()
                    .getRed());
            buf.writeInt(
                target.getColor()
                    .getGreen());
            buf.writeInt(
                target.getColor()
                    .getBlue());
        }
        buf.writeBoolean(this.shouldLookAtTerminal);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.sideOrdinal);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        if (buf.readableBytes() == LEGACY_TARGET_PACKET_BYTES) {
            this.rendererCubeTargets = new ArrayList<>(SINGLE_TARGET_CAPACITY);
            this.shouldLookAtTerminal = true;
            this.x = buf.readInt();
            this.y = buf.readInt();
            this.z = buf.readInt();
            this.sideOrdinal = buf.readInt();
            this.rendererCubeTargets.add(new RendererCubeTarget(new BlockPos(this.x, this.y, this.z), AE2_HIGHLIGHT));
            return;
        }

        int encodedTargets = Math.max(buf.readInt(), 0);
        int storedTargets = Math.min(encodedTargets, MAX_RENDER_TARGETS);
        this.rendererCubeTargets = new ArrayList<>(storedTargets);
        for (int i = 0; i < encodedTargets; i++) {
            BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            Color color = new Color(buf.readInt(), buf.readInt(), buf.readInt());
            if (i < storedTargets) {
                this.rendererCubeTargets.add(new RendererCubeTarget(pos, color));
            }
        }
        this.shouldLookAtTerminal = buf.readBoolean();
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.sideOrdinal = buf.readInt();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        if (!this.rendererCubeTargets.isEmpty()) {
            RendererCube.INSTANCE.drawMerged(this.rendererCubeTargets);
        }
        if (this.shouldLookAtTerminal) {
            lookAtTerminal();
            closePlayerInventory();
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private void closePlayerInventory() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiInventory)) {
            return;
        }

        if (mc.thePlayer != null) {
            mc.thePlayer.closeScreen();
        }
        mc.displayGuiScreen(null);
        mc.setIngameFocus();
    }

    @SideOnly(Side.CLIENT)
    private void lookAtTerminal() {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return;
        }

        ForgeDirection side = ForgeDirection.getOrientation(this.sideOrdinal);
        double targetX = centeredFaceCoordinate(this.x, side.offsetX);
        double targetY = centeredFaceCoordinate(this.y, side.offsetY);
        double targetZ = centeredFaceCoordinate(this.z, side.offsetZ);

        double dx = targetX - player.posX;
        double dy = targetY - (player.posY + player.getEyeHeight());
        double dz = targetZ - player.posZ;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - YAW_OFFSET_DEGREES);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontalDistance)));

        player.rotationYaw = yaw;
        player.prevRotationYaw = yaw;
        player.rotationYawHead = yaw;
        player.prevRotationYawHead = yaw;
        player.renderYawOffset = yaw;
        player.rotationPitch = pitch;
        player.prevRotationPitch = pitch;
    }

    private static double centeredFaceCoordinate(int blockCoordinate, int sideOffset) {
        return blockCoordinate + BLOCK_CENTER_OFFSET + sideOffset * FACE_OFFSET;
    }
}
