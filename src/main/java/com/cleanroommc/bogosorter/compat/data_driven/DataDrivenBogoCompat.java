package com.cleanroommc.bogosorter.compat.data_driven;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipFile;

/**
 * @author ZZZank
 */
public class DataDrivenBogoCompat {
    public static final String COMPAT_FILE = "bogo.compat.json";
    private static final Gson GSON = new Gson();
    private static boolean init = false;

    public static void handle(IBogoSortAPI api) {
        if (init) {
            return;
        }
        init = true;
        for (BogoCompatHandler handler : scanDefault()) {
            try {
                handler.handle(api);
            } catch (Exception ignored) {
            }
        }
    }

    public static ArrayList<BogoCompatHandler> scanDefault() {
        var parsed = new ArrayList<BogoCompatHandler>();
        for (ModContainer mod : Loader.instance().getModList()) {
            try (var zip = new ZipFile(mod.getSource())) {
                var entry = zip.getEntry("resources/" + COMPAT_FILE);
                if (entry == null) {
                    continue;
                }
                var arr = GSON.fromJson(
                    new BufferedReader(new InputStreamReader(zip.getInputStream(entry))),
                    JsonArray.class
                );
                parsed.addAll(parseAll(arr));
            } catch (IOException ignored) {
            }
        }
        Path path = Loader.instance().getConfigDir().toPath().resolve("bogosorter/" + COMPAT_FILE);
        if (Files.exists(path)) {
            try {
                var arr = GSON.fromJson(Files.newBufferedReader(path), JsonArray.class);
                parsed.addAll(parseAll(arr));
            } catch (IOException ignored) {
            }
        }
        return parsed;
    }

    private static ArrayList<BogoCompatHandler> parseAll(JsonArray all) {
        var parsed = new ArrayList<BogoCompatHandler>();
        for (JsonElement element : all) {
            try {
                parsed.add(BogoCompatParser.parse(element.getAsJsonObject()));
            } catch (Exception e) {
            }
        }
        return parsed;
    }
}
