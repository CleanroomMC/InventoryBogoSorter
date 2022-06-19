package com.cleanroommc.bogosorter.mixin.gtce;

import com.cleanroommc.bogosorter.compat.gtce.IModularSortable;
import com.cleanroommc.bogosorter.compat.gtce.SortableSlotWidget;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.common.metatileentities.storage.MetaTileEntityChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MetaTileEntityChest.class, remap = false)
public abstract class MixinChest {

    @Shadow
    @Final
    private int rowSize;

    @Shadow
    @Final
    private int amountOfRows;

    @Shadow
    private ItemStackHandler inventory;

    @Shadow
    protected abstract void onContainerOpen(EntityPlayer player);

    @Shadow
    protected abstract void onContainerClose(EntityPlayer player);

    public MetaTileEntityChest getChest() {
        return (MetaTileEntityChest) (Object) this;
    }

    /**
     * @author brachy84
     */
    @Overwrite
    public ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, Math.max(176, 14 + rowSize * 18), 18 + 18 * amountOfRows + 94).label(5, 5, getChest().getMetaFullName());

        int y;
        for (y = 0; y < this.amountOfRows; ++y) {
            for (int x = 0; x < this.rowSize; ++x) {
                int index = y * this.rowSize + x;
                builder.widget(new SortableSlotWidget(inventory, index, 7 + x * 18, 18 + y * 18)
                        .setSortArea("chest")
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        y = (Math.max(176, 14 + this.rowSize * 18) - 162) / 2;
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, y, 18 + 18 * this.amountOfRows + 12);
        if (!getChest().getWorld().isRemote) {
            builder.bindOpenListener(() -> {
                onContainerOpen(entityPlayer);
            });
            builder.bindCloseListener(() -> {
                onContainerClose(entityPlayer);
            });
        }

        ModularUI modularUI = builder.build(getChest().getHolder(), entityPlayer);
        ((IModularSortable) (Object) modularUI).addSortArea("chest", rowSize);
        return  modularUI;
    }
}
