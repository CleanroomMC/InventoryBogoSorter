package com.cleanroommc.bogosorter.compat.gtce;

import com.cleanroommc.bogosorter.BogoSorter;
import com.github.bsideup.jabel.Desugar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTCompat {
    @Nullable
    private static final Class<?> toolClass;
    @Nullable
    private static final GT gt;

    @Desugar
    private record GT(String modName, String toolClassName, String statsTag, String materialKey) {
        GT(String modName, String toolClassName, String statsTag) {
            this(modName, toolClassName, statsTag, "Material");
        }
        GT(String modName) {
            this(modName, "gregtech.common.items.MetaTool", "GT.ToolStats");
        }
    }

    /** GregTech Community Edition */
    private static final GT GTCE  = new GT("GregTech Community Edition");
    /** GregTech Nomifactory Edition */
    private static final GT GTNE  = new GT("GregTech Nomifactory Edition");
    /** GregTech Community Edition Unofficial */
    private static final GT GTCEu = new GT("GregTech", "gregtech.api.items.toolitem.IGTTool", "GT.Tool");

    static {
        // All three GregTechs use the ModID "gregtech"
        BogoSorter.Mods mod = BogoSorter.Mods.GT_ANY;

        // Determine which GregTech is loaded
        // no GT loaded
        if(Loader.isModLoaded(mod.id)) {
            ModContainer m = Loader.instance().getIndexedModList().get(mod.id);
            if(isGTCE(m))
                gt = GTCE;
            else if(isGTNE(m))
                gt = GTNE;
            else if(isGTCEu(m))
                gt = GTCEu;
            else // unsupported
                gt = null;
        } else
            gt = null;

        // Get the appropriate tool class from the loaded mod, if any
        Class<?> clazz = null;
        if(gt != null) {
            try {
                clazz = Class.forName(gt.toolClassName, false, GTCompat.class.getClassLoader());
            } catch(Exception ignored) {
            }
        }
        toolClass = clazz;
    }

    public static boolean isGTTool(ItemStack itemStack) {
        return toolClass != null && toolClass.isAssignableFrom(itemStack.getItem().getClass());
    }

    @NotNull
    public static String getGtToolMaterial(ItemStack itemStack) {
        if(gt != null && isGTTool(itemStack)) {
            NBTTagCompound statsTag;
            statsTag = itemStack.getSubCompound(gt.statsTag);

            if (statsTag == null)
                return "";
            if (statsTag.hasKey(gt.materialKey))
                return statsTag.getString(gt.materialKey);
        }
        return "";
    }

    public static boolean isGTCEu(ModContainer m) {
        return GTCEu.modName.equals(m.getMetadata().name);
    }

    public static boolean isGTCE(ModContainer m) {
        return GTCE.modName.equals(m.getMetadata().name);
    }

    public static boolean isGTNE(ModContainer m) {
        return GTNE.modName.equals(m.getMetadata().name);
    }
}
