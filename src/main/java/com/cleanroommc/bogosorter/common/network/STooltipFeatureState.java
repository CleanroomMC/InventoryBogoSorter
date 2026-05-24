package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.common.config.TooltipFeatureConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class STooltipFeatureState implements IPacket {

    private boolean enabled;

    public STooltipFeatureState() {}

    public STooltipFeatureState(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeBoolean(this.enabled);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.enabled = buf.readBoolean();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        TooltipFeatureConfig.setTooltipEnabled(this.enabled);
        if (!this.enabled) {
            resetAe2TooltipClient();
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static void resetAe2TooltipClient() {
        try {
            Class<?> tooltipClient = Class.forName("com.cleanroommc.bogosorter.compat.nei.Ae2TooltipClient");
            tooltipClient.getMethod("resetAe2State")
                .invoke(null);
        } catch (Throwable ignored) {}
    }
}
