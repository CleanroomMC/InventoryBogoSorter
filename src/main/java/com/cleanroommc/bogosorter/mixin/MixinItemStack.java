package com.cleanroommc.bogosorter.mixin;

import com.cleanroommc.bogosorter.BogoSorter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class MixinItemStack
{
    @Shadow
    private Item item;

    @Shadow
    private int itemDamage;

    @Shadow
    private int getMaxDamage() {return 0;}

    @Inject(method = "damageItem", at = @At("RETURN"))
    private void damageItem(int amount, EntityLivingBase entityIn, CallbackInfo ci) {
        if (!(entityIn instanceof EntityPlayer)) {
            return;
        }
        if (item instanceof ItemArmor) {
            return;
        }

        if (itemDamage == getMaxDamage()){
            BogoSorter.LOGGER.info("Last Hit");
        }else {
            BogoSorter.LOGGER.info("Item Damaged");
        }

    }
}
