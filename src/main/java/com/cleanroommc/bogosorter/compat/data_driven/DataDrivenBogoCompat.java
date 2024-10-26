package com.cleanroommc.bogosorter.compat.data_driven;

import com.cleanroommc.bogosorter.BogoSorter;
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

    public static void handle(IBogoSortAPI api) {
        BogoSorter.LOGGER.info("adding data-driven compat");
        for (BogoCompatHandler handler : scanDefault()) {
            try {
                handler.handle(api);
            } catch (Exception e) {
                BogoSorter.LOGGER.error("error when adding data-driven compat",  e);
            }
        }
    }

    public static ArrayList<BogoCompatHandler> scanDefault() {
        var parsed = new ArrayList<BogoCompatHandler>();
        for (ModContainer mod : Loader.instance().getModList()) {
            var f = mod.getSource();
            if (!f.exists()) {
                continue; //for some special mods like 'minecraft' or 'scalar' or 'mcp'
            }
            try (var zip = new ZipFile(f)) {
                var entry = zip.getEntry(COMPAT_FILE);
                if (entry == null) {
                    continue;
                }
                BogoSorter.LOGGER.info("found '{}' in mod '{}'", COMPAT_FILE, mod.getModId());
                var arr = GSON.fromJson(
                    new BufferedReader(new InputStreamReader(zip.getInputStream(entry))),
                    JsonArray.class
                );
                parsed.addAll(parseAll(arr));
            } catch (IOException e) {
                BogoSorter.LOGGER.error("error during reading '{}' in mod '{}'", COMPAT_FILE, mod.getModId(), e);
            }
        }
        Path path = Loader.instance().getConfigDir().toPath().resolve("bogosorter").resolve(COMPAT_FILE);
        if (Files.exists(path)) {
            BogoSorter.LOGGER.info("found compat file in config directory");
            try {
                var arr = GSON.fromJson(Files.newBufferedReader(path), JsonArray.class);
                parsed.addAll(parseAll(arr));
            } catch (IOException e) {
                BogoSorter.LOGGER.error("error when parsing compat file from config", e);
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
                BogoSorter.LOGGER.error("error when parsing handler json: {}", element,  e);
            }
        }
        return parsed;
    }
}
