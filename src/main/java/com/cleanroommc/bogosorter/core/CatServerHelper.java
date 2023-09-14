package com.cleanroommc.bogosorter.core;

import com.cleanroommc.bogosorter.BogoSorter;

public class CatServerHelper {

    private static boolean loaded = false;
    private static boolean init = false;

    public static boolean isCatServerLoaded() {
        if (!init) {
            loaded = classExists("catserver.server.CatServerLaunch");
            if (loaded) BogoSorter.LOGGER.info("### Detected CatServer ###");
            init = true;
        }
        return loaded;
    }

    public static boolean classExists(String name) {
        try {
            Class.forName(name, false, CatServerHelper.class.getClassLoader());
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
