package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.network.NetworkUtils;
import com.cleanroommc.bogosorter.common.sort.SortHandler;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Serializer {

    public static final JsonParser jsonParser = new JsonParser();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final String cfgPath = Loader.instance().getConfigDir().toString();
    public static final File configJsonPath = new File(cfgPath + path("", "bogosorter", "config.json"));
    public static final File orePrefixJsonPath = new File(cfgPath + path("", "bogosorter", "orePrefix.json"));

    private static String path(String... path) {
        return StringUtils.join(path, File.separatorChar);
    }

    private Serializer() {
    }

    @SideOnly(Side.CLIENT)
    public static void saveConfig() {
        JsonObject json = new JsonObject();
        BogoSorterConfig.save(json);
        saveJson(configJsonPath, json);
    }

    public static void loadConfig() {
        if (NetworkUtils.isDedicatedClient()) {
            if (!Files.exists(configJsonPath.toPath())) {
                BogoSorterConfig.loadDefaultRules();
                saveConfig();
            }
            JsonElement jsonElement = loadJson(configJsonPath);
            if (jsonElement == null || !jsonElement.isJsonObject()) {
                BogoSorter.LOGGER.error("Error loading config!");
            } else {
                BogoSorterConfig.load(jsonElement.getAsJsonObject());
                SortHandler.cacheItemSortRules.put(Minecraft.getMinecraft().player, BogoSorterConfig.sortRules);
                SortHandler.cacheNbtSortRules.put(Minecraft.getMinecraft().player, BogoSorterConfig.nbtSortRules);
            }
        }

        if (!Files.exists(orePrefixJsonPath.toPath())) {
            saveOrePrefixes();
            return;
        }
        JsonElement jsonElement = loadJson(orePrefixJsonPath);
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            BogoSorter.LOGGER.error("Error loading ore prefix config!");
            return;
        }
        BogoSorterConfig.loadOrePrefixes(jsonElement.getAsJsonObject());
    }

    public static void saveOrePrefixes() {
        JsonObject json = new JsonObject();
        BogoSorterConfig.saveOrePrefixes(json);
        saveJson(orePrefixJsonPath, json);
    }

    public static JsonElement loadJson(File file) {
        try {
            if (!file.isFile()) return null;
            Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            JsonElement json = jsonParser.parse(new JsonReader(reader));
            reader.close();
            return json;
        } catch (Exception e) {
            BogoSorter.LOGGER.error("Failed to read file on path {}", file, e);
        }
        return null;
    }

    public static boolean saveJson(File file, JsonElement element) {
        try {
            if (!file.getParentFile().isDirectory()) {
                if (!file.getParentFile().mkdirs()) {
                    BogoSorter.LOGGER.error("Failed to create file dirs on path {}", file);
                }
            }
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            writer.write(gson.toJson(element));
            writer.close();
            return true;
        } catch (Exception e) {
            BogoSorter.LOGGER.error("Failed to save file on path {}", file, e);
        }
        return false;
    }

}
