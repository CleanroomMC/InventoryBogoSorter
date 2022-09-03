package com.cleanroommc.bogosorter.mixin.gtceu;

import com.cleanroommc.bogosorter.compat.gtce.IModularSortable;
import com.cleanroommc.bogosorter.compat.gtce.SortableSlotWidget;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.common.metatileentities.storage.MetaTileEntityCrate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MetaTileEntityCrate.class, remap = false)
public class MixinMetaTileEntityCrate {

    @Shadow
    @Final
    private int inventorySize;

    @Shadow
    private ItemStackHandler inventory;

    /**
     * @author brachy84
     */
    @Overwrite
    public ModularUI createUI(EntityPlayer entityPlayer) {
        int factor = inventorySize / 9 > 8 ? 18 : 9;
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176 + (factor == 18 ? 176 : 0), 8 + this.inventorySize / factor * 18 + 104).label(5, 5, bogosorter$getThis().getMetaFullName());

        for (int i = 0; i < this.inventorySize; ++i) {
            builder.widget(new SortableSlotWidget(inventory, i, 7 * (factor == 18 ? 2 : 1) + i % factor * 18, 18 + i / factor * 18)
                    .setSortArea("chest")
                    .setBackgroundTexture(new TextureArea[]{GuiTextures.SLOT}));
        }

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7 + (factor == 18 ? 88 : 0), 18 + this.inventorySize / factor * 18 + 11);
        ModularUI modularUI = builder.build(bogosorter$getThis().getHolder(), entityPlayer);
        ((IModularSortable) (Object) modularUI).addSortArea("chest", factor);
        return modularUI;
    }

    private MetaTileEntityCrate bogosorter$getThis() {
        return (MetaTileEntityCrate) (Object) (this);
    }
}
