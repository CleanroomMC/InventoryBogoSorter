package com.cleanroommc.bogosorter.compat.data_driven;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.compat.data_driven.handler.BogoCompatHandler;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.minecraftforge.fml.common.Loader;

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

    public static ArrayList<BogoCompatHandler> scanHandlers() {
        var parsed = new ArrayList<BogoCompatHandler>();

        // mod
        for (var mod : Loader.instance().getModList()) {
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
                BogoSorter.LOGGER.error("IO error during reading '{}' in mod '{}'", COMPAT_FILE, mod.getModId(), e);
            }
        }

        // path
        Path path = Loader.instance().getConfigDir().toPath().resolve("bogosorter").resolve(COMPAT_FILE);
        if (Files.exists(path)) {
            BogoSorter.LOGGER.info("found compat file in config directory");
            JsonArray arr;
            try {
                arr = GSON.fromJson(Files.newBufferedReader(path), JsonArray.class);
            } catch (IOException e) {
                BogoSorter.LOGGER.error("IO error when reading compat file from config", e);
                arr = null;
            }
            if (arr != null) {
                parsed.addAll(parseAll(arr));
            }
        }

        return parsed;
    }

    private static ArrayList<BogoCompatHandler> parseAll(JsonArray all) {
        var parsed = new ArrayList<BogoCompatHandler>();
        for (var element : all) {
            try {
                parsed.add(BogoCompatHandler.SCHEMA.read(element.getAsJsonObject()));
            } catch (Exception e) {
                BogoSorter.LOGGER.error("error when parsing handler json: {}", element,  e);
            }
        }
        return parsed;
    }
}
