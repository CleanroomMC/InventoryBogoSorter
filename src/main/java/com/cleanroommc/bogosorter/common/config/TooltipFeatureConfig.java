package com.cleanroommc.bogosorter.common.config;

import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

public final class TooltipFeatureConfig {

    private TooltipFeatureConfig() {}

    public static boolean isTooltipEnabled() {
        return BogoSorterConfig.ae2Integration.enableTooltipSearch;
    }

    public static void setTooltipEnabled(boolean enabled) {
        BogoSorterConfig.ae2Integration.enableTooltipSearch = enabled;
    }

    public static void load() {}

    public static void save() {
        ConfigurationManager.save(BogoSorterConfig.class);
    }
}
