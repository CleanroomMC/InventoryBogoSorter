package com.cleanroommc.bogosorter.core.mixin;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.common.lock.LockSlotCapability;
import com.cleanroommc.bogosorter.common.lock.SlotLock;
import com.cleanroommc.bogosorter.common.sort.IGuiContainerAccessor;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiContainer.class)
public class GuiContainerMixin extends GuiScreen implements IGuiContainerAccessor {

    @Override
    public List<GuiButton> getButtons() {
        return buttonList;
    }

    @Inject(method = "drawSlot", at = @At("TAIL"))
    public void injectDrawLock(Slot slotIn, CallbackInfo ci) {
        LockSlotCapability cap = SlotLock.getClientCap();
        if (cap != null && BogoSortAPI.isPlayerSlot(slotIn) && cap.isSlotLocked(slotIn.getSlotIndex())) {
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            SlotLock.drawLock(slotIn.xPos, slotIn.yPos, 16, 16);
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
        }
    }

    @WrapOperation(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;canDragIntoSlot(Lnet/minecraft/inventory/Slot;)Z"))
    public boolean injectDragCheck(Container instance, Slot slotIn, Operation<Boolean> original) {
        return original.call(instance, slotIn) && !SlotLock.getClientCap().isSlotLocked(slotIn);
    }
}
