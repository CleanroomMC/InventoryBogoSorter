package com.cleanroommc.bogosorter.core.mixin;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.inventory.GuiContainerCreative$CreativeSlot")
public class CreativeSlotMixin extends Slot{

    private CreativeSlotMixin(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void constructor(GuiContainerCreative this$0, Slot p_i46313_2_, int index, CallbackInfo ci) {
        slotNumber = p_i46313_2_.slotNumber;
    }
}
