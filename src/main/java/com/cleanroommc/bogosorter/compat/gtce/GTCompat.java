package com.cleanroommc.bogosorter.compat.gtce;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.ModContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTCompat {
    @Nullable
    private static final Class<?> versionClass;
    @Nullable
    private static final Class<?> toolClass;
    @Nullable
    private static final GT gt;

    @Desugar
    private record GT(String toolClassName, String statsTag, String materialKey) {
        GT(String toolClassName, String statsTag) {
            this(toolClassName, statsTag, "Material");
        }
        GT() {
            this("gregtech.common.items.MetaTool", "GT.ToolStats");
        }
    }

    /** GregTech Community Edition */
    private static final GT GTCE  = new GT();
    /** GregTech Nomifactory Edition */
    private static final GT GTNE  = new GT();
    /** GregTech Community Edition Unofficial */
    private static final GT GTCEu = new GT("gregtech.api.items.toolitem.IGTTool", "GT.Tool");

    static {
        Class<?> clazz;

        // dynamically load the version info, otherwise it's a constant recorded at compile time
        try {
            clazz = Class.forName("gregtech.GregTechVersion", false, GTCompat.class.getClassLoader());
        } catch(Exception ignored) {
            clazz = null;
        }
        versionClass = clazz;

        // Determine which GregTech is loaded
        if(versionClass == null)
            gt = null;
        else {
            GT detected = null;
            try {
                int major = versionClass.getField("MAJOR").getInt(null);
                int minor = versionClass.getField("MINOR").getInt(null);
                // GTNE started with 1.18 and commits to staying on major version 1
                if (major == 1 && minor > 17)
                    detected = GTNE;
                // GTCE reached EOL on version 1.17.1.770
                else if(major <= 1)
                    detected = GTCE;
                else // GTCEu uses 2.0+
                    detected = GTCEu;
            } catch(Exception ignored) {
            } finally {
                gt = detected;
            }
        }

        // Get the appropriate tool class from the loaded mod, if any
        if(gt != null) {
            try {
                clazz = Class.forName(gt.toolClassName, false, GTCompat.class.getClassLoader());
            } catch(Exception ignored) {
                clazz = null;
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

    /** @param m unused */
    public static boolean isGTCEu(ModContainer m) {
        return GTCEu == gt;
    }

    /** @param m unused */
    public static boolean isGTCE(ModContainer m) {
        return GTCE == gt;
    }

    /** @param m unused */
    public static boolean isGTNE(ModContainer m) {
        return GTNE == gt;
    }
}
