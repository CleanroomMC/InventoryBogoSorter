package com.cleanroommc.bogosorter.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public class SRefillSound implements IPacket {

    @Override
    public void encode(PacketBuffer buf) throws IOException {
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_CHICKEN_EGG, 1, 1));
        return null;
    }
}
