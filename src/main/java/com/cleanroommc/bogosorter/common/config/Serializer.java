package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Serializer {

    public static final JsonParser jsonParser = new JsonParser();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public final String cfgPath = Loader.instance().getConfigDir().toString();
    public final File configJsonPath = new File(cfgPath + "\\bogosorter\\config.json");

    public void saveConfig() {
        JsonObject json = new JsonObject();
        writeItemSortRules(json);
        writeNbtSortRules(json);
        saveJson(configJsonPath, json);
    }

    public void loadConfig() {
        if (!Files.exists(configJsonPath.toPath())) {
            saveConfig();
        }
        JsonElement jsonElement = loadJson(configJsonPath);
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            BogoSorter.LOGGER.error("Error loading config!");
            return;
        }
        readRules(jsonElement.getAsJsonObject());
    }

    public void readRules(JsonObject json) {
        SortHandler.getItemSortRules().clear();
        if (json.has("ItemSortRules")) {
            JsonArray sortRules = json.getAsJsonArray("ItemSortRules");
            for (JsonElement jsonElement : sortRules) {
                SortRule<ItemStack> rule = BogoSortAPI.INSTANCE.getItemSortRule(jsonElement.getAsString());
                if (rule == null) {
                    BogoSorter.LOGGER.error("Could not find item sort rule with key '{}'.", jsonElement.getAsString());
                } else {
                    SortHandler.getItemSortRules().add(rule);
                }
            }
        }
        SortHandler.getNbtSortRules().clear();
        if (json.has("NbtSortRules")) {
            JsonArray sortRules = json.getAsJsonArray("NbtSortRules");
            for (JsonElement jsonElement : sortRules) {
                NbtSortRule rule = BogoSortAPI.INSTANCE.getNbtSortRule(jsonElement.getAsString());
                if (rule == null) {
                    BogoSorter.LOGGER.error("Could not find nbt sort rule with key '{}'.", jsonElement.getAsString());
                } else {
                    SortHandler.getNbtSortRules().add(rule);
                }
            }
        }
    }

    public void writeItemSortRules(JsonObject json) {
        JsonArray jsonRules = new JsonArray();
        for (SortRule<ItemStack> rule : SortHandler.getItemSortRules()) {
            jsonRules.add(rule.getKey());
        }
        json.add("ItemSortRules", jsonRules);
    }

    public void writeNbtSortRules(JsonObject json) {
        JsonArray jsonRules = new JsonArray();
        for (NbtSortRule rule : SortHandler.getNbtSortRules()) {
            jsonRules.add(rule.getKey());
        }
        json.add("NbtSortRules", jsonRules);
    }

    /**
     * Tries to extract <code>JsonObject</code> from file on given path
     *
     * @param filePath path to file
     * @return <code>JsonObject</code> if extraction succeeds; otherwise <code>null</code>
     */
    public static JsonObject tryExtractFromFile(Path filePath) {
        try (InputStream fileStream = Files.newInputStream(filePath)) {
            InputStreamReader streamReader = new InputStreamReader(fileStream);
            return jsonParser.parse(streamReader).getAsJsonObject();
        } catch (IOException exception) {
            BogoSorter.LOGGER.error("Failed to read file on path {}", filePath, exception);
        } catch (JsonParseException exception) {
            BogoSorter.LOGGER.error("Failed to extract json from file", exception);
        } catch (Exception exception) {
            BogoSorter.LOGGER.error("Failed to extract json from file on path {}", filePath, exception);
        }

        return null;
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
