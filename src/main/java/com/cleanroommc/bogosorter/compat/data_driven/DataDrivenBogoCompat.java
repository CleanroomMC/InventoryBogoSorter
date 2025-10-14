package com.cleanroommc.bogosorter.compat.data_driven;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.handler.BogoCompatHandler;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.Loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipFile;

/**
 * @author ZZZank
 */
public class DataDrivenBogoCompat {
    public static final String COMPAT_FILE = "bogo.compat.json";
    private static final String COND_FIELD_NAME = "condition";
    private static final JsonSchema<BogoCondition> CONDITION_OBJECT_SCHEMA = JsonSchema.object(
        BogoCondition.SCHEMA
            .describe("The action will be applied when the condition returned `true`")
            .toOptionalField(COND_FIELD_NAME),
        cond -> cond.orElse(BogoCondition.ALWAYS)
    );
    public static final JsonSchema<Optional<BogoCompatHandler>> SCHEMA_SINGLE = new JsonSchema<>() {
        @Override
        public Optional<BogoCompatHandler> read(JsonElement json) {
            if (CONDITION_OBJECT_SCHEMA.read(json).test()) {
                return Optional.of(BogoCompatHandler.SCHEMA.read(json));
            }
            return Optional.empty();
        }

        @Override
        public JsonObject getSchema(Map<String, JsonSchema<?>> definitions) {
            var condSchema = BogoCondition.SCHEMA.getSchema(definitions);
            var schema = BogoCompatHandler.SCHEMA.getSchema(definitions);
            schema.get("properties").getAsJsonObject().add(COND_FIELD_NAME, condSchema);
            return schema;
        }
    };
    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        System.out.println(GSON.toJson(generateJsonSchema()));

        validateCompatFile(Paths.get("src/main/resources/bogo.compat.json"));
    }

    public static ArrayList<BogoCompatHandler> scanHandlers() {
        var parsed = new ArrayList<BogoCompatHandler>();

        // mod
        for (var mod : Loader.instance().getModList()) {
            var f = mod.getSource();
            if (f == null || !f.exists()) {
                continue; //for some special mods like 'minecraft' or 'scalar' or 'mcp'
            }
            try (var zip = new ZipFile(f)) {
                var entry = zip.getEntry(COMPAT_FILE);
                if (entry == null) {
                    continue;
                }
                BogoSorter.LOGGER.info("found '{}' in mod '{}'", COMPAT_FILE, mod.getModId());
                var json = GSON.fromJson(
                    new BufferedReader(new InputStreamReader(zip.getInputStream(entry))),
                    JsonObject.class
                );
                parsed.addAll(parseAll(json));
            } catch (IOException e) {
                BogoSorter.LOGGER.error("IO error during reading '{}' in mod '{}'", COMPAT_FILE, mod.getModId(), e);
            }
        }

        // path
        Path path = Loader.instance().getConfigDir().toPath().resolve("bogosorter").resolve(COMPAT_FILE);
        if (Files.exists(path)) {
            BogoSorter.LOGGER.info("found compat file in config directory");
            try {
                var json = GSON.fromJson(Files.newBufferedReader(path), JsonObject.class);
                parsed.addAll(parseAll(json));
            } catch (IOException e) {
                BogoSorter.LOGGER.error("IO error when reading compat file from config", e);
            }
        }

        return parsed;
    }

    private static ArrayList<BogoCompatHandler> parseAll(JsonObject obj) {
        var parsed = new ArrayList<BogoCompatHandler>();
        for (var json : obj.get("actions").getAsJsonArray()) {
            try {
                SCHEMA_SINGLE.read(json).ifPresent(parsed::add);
            } catch (Exception e) {
                BogoSorter.LOGGER.error("error when parsing handler json: {}", json,  e);
            }
        }
        return parsed;
    }

    /// generate JSON Schema for Bogo compat file
    private static JsonObject generateJsonSchema() {
        // use object to allow adding "$schema"
        var jsonSchema = JsonSchema.object(
            SCHEMA_SINGLE
                .extractToDefinitions("action")
                .toList()
                .toField("actions"),
            i -> i
        );
        return jsonSchema.getSchema();
    }

    /// parse and do basic validation on a Bogo compat file.
    ///
    /// Exceptions will be thrown if anything goes wrong
    private static void validateCompatFile(Path path) {
        try (var reader = Files.newBufferedReader(path)) {
            var json = GSON.fromJson(reader, JsonObject.class);
            var handlers = parseAll(json);

            var api = IBogoSortAPI.getInstance();
            for (var handler : handlers) {
                handler.handle(api);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
