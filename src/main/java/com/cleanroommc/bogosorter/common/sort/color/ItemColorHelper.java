package com.cleanroommc.bogosorter.common.sort.color;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.modularui.api.math.Color;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public class ItemColorHelper {

    private static final Object2IntMap<ItemStack> ITEM_COLORS = new Object2IntOpenCustomHashMap<>(BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);

    public static int getHue(int color) {
        if (color == 0) return 0;
        float r = Color.getRedF(color);
        float g = Color.getGreenF(color);
        float b = Color.getBlueF(color);
        float min = Math.min(r, Math.min(g, b));
        float hue;
        if (r > g && r > b) {
            hue = (g - b) / (r - min);
        } else if (g > r && g > b) {
            hue = 2 + (b - r) / (g - min);
        } else if (b > r && b > g) {
            hue = 4 + (r - g) / (b - min);
        } else {
            return 0;
        }
        hue *= 60;
        if (hue < 0) {
            hue += 360;
        }
        return (int) hue;
    }

    public static int getItemColorHue(ItemStack item) {
        return getHue(getAverageItemColor(item));
    }

    public static int getAverageItemColor(ItemStack item) {
        if (item.isEmpty()) {
            return 0;
        }
        if (ITEM_COLORS.containsKey(item)) {
            return ITEM_COLORS.getInt(item);
        }
        int color;
        if (item.getItem() instanceof ItemBlock) {
            color = getBlockColors(item, ((ItemBlock) item.getItem()).getBlock());
        } else {
            color = getItemColors(item);
        }
        ITEM_COLORS.put(item, color);
        return color;
    }

    private static int getItemColors(ItemStack itemStack) {
        final ItemColors itemColors = Minecraft.getMinecraft().getItemColors();
        final int renderColor = itemColors.colorMultiplier(itemStack, 0);
        final TextureAtlasSprite textureAtlasSprite = getTextureAtlasSprite(itemStack);
        return getColors(textureAtlasSprite, renderColor);
    }

    private static int getBlockColors(ItemStack itemStack, Block block) {
        final int meta = itemStack.getMetadata();
        IBlockState blockState;
        try {
            blockState = block.getStateFromMeta(meta);
        } catch (RuntimeException | LinkageError ignored) {
            blockState = block.getDefaultState();
        }

        final BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
        int renderColor = blockColors.colorMultiplier(blockState, null, null, 0);
        final TextureAtlasSprite textureAtlasSprite;
        if (BogoSorter.isGTCEuLoaded() && blockState.getBlock() instanceof BlockMachine) {
            MetaTileEntity mte = GTUtility.getMetaTileEntity(itemStack);
            Pair<TextureAtlasSprite, Integer> pair = mte.getParticleTexture();
            textureAtlasSprite = pair.getKey();
            renderColor = pair.getRight();

        } else {
            textureAtlasSprite = getTextureAtlasSprite(blockState);
        }
        return getColors(textureAtlasSprite, renderColor);
    }

    public static int getColors(TextureAtlasSprite textureAtlasSprite, int renderColor) {
        int[] color;
        if (textureAtlasSprite == null) {
            color = new int[]{0, 0, 0};
        } else {
            final BufferedImage bufferedImage = getBufferedImage(textureAtlasSprite);
            if (bufferedImage == null) {
                color = new int[]{0, 0, 0};
            } else {
                color = ColorThief.getColor(bufferedImage, 10, true);
            }
        }
        if (color == null) return 0;
        int red = (int) ((color[0] - 1) * (float) (renderColor >> 16 & 255) / 255.0F);
        int green = (int) ((color[1] - 1) * (float) (renderColor >> 8 & 255) / 255.0F);
        int blue = (int) ((color[2] - 1) * (float) (renderColor & 255) / 255.0F);
        red = MathHelper.clamp(red, 0, 255);
        green = MathHelper.clamp(green, 0, 255);
        blue = MathHelper.clamp(blue, 0, 255);
        return Color.rgb(red, green, blue);
    }

    @Nullable
    private static BufferedImage getBufferedImage(TextureAtlasSprite textureAtlasSprite) {
        final int iconWidth = textureAtlasSprite.getIconWidth();
        final int iconHeight = textureAtlasSprite.getIconHeight();
        final int frameCount = textureAtlasSprite.getFrameCount();
        if (iconWidth <= 0 || iconHeight <= 0 || frameCount <= 0) {
            return null;
        }

        BufferedImage bufferedImage = new BufferedImage(iconWidth, iconHeight * frameCount, BufferedImage.TYPE_4BYTE_ABGR);
        for (int i = 0; i < frameCount; i++) {
            int[][] frameTextureData = textureAtlasSprite.getFrameTextureData(i);
            int[] largestMipMapTextureData = frameTextureData[0];
            bufferedImage.setRGB(0, i * iconHeight, iconWidth, iconHeight, largestMipMapTextureData, 0, iconWidth);
        }

        return bufferedImage;
    }

    @Nullable
    private static TextureAtlasSprite getTextureAtlasSprite(IBlockState blockState) {
        Minecraft minecraft = Minecraft.getMinecraft();
        BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRendererDispatcher();
        BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
        TextureAtlasSprite textureAtlasSprite = blockModelShapes.getTexture(blockState);
        if (textureAtlasSprite == minecraft.getTextureMapBlocks().getMissingSprite()) {
            return null;
        }
        return textureAtlasSprite;
    }

    private static TextureAtlasSprite getTextureAtlasSprite(ItemStack itemStack) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        ItemModelMesher itemModelMesher = renderItem.getItemModelMesher();
        IBakedModel itemModel = itemModelMesher.getItemModel(itemStack);
        return itemModel.getParticleTexture();
    }
}
