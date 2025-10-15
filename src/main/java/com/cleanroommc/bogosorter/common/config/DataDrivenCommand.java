package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.compat.data_driven.DataDrivenBogoCompat;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.server.command.CommandTreeBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author ZZZank
 */
public class DataDrivenCommand extends CommandTreeBase {

    private static final String PREFIX = "/bogosorter datadriven";

    {
        addSubcommand(new DataDrivenInitCommand());
        addSubcommand(new DataDrivenSyntaxCommand());
    }

    @Override
    public String getName() {
        return "datadriven";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return PREFIX + " [init|syntax]";
    }

    private static Path getConfigFolder() {
        return Loader.instance().getConfigDir().toPath().resolve("bogosorter");
    }

    private static class DataDrivenInitCommand extends CommandBase {

        @Override
        public String getName() {
            return "init";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return PREFIX + " init";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            // schema
            sender.sendMessage(new TextComponentTranslation("bogosort.command.datadriven.init.schema"));
            try (var writer = Files.newBufferedWriter(getConfigFolder().resolve("bogo.compat.schema.json"))) {
                var generated = DataDrivenBogoCompat.generateJsonSchema();

                // These commands will not be used frequently, so we don't need to cache Gson
                new GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .toJson(generated, writer);
            } catch (IOException e) {
                throw new CommandException(e.toString());
            }
            // template file
            if (!Files.exists(getConfigFolder().resolve("bogo.compat.json"))) {
                sender.sendMessage(new TextComponentTranslation("bogosort.command.datadriven.init.template"));
                try (var writer = Files.newBufferedWriter(getConfigFolder().resolve("bogo.compat.json"))) {
                    var generated = new JsonObject();
                    generated.addProperty("$schema", "./bogo.compat.schema.json");
                    generated.add("actions", new JsonArray());

                    new GsonBuilder()
                            .setPrettyPrinting()
                            .create()
                            .toJson(generated, writer);
                } catch (IOException e) {
                    throw new CommandException(e.toString());
                }
            }
        }
    }

    private static class DataDrivenSyntaxCommand extends CommandBase {

        @Override
        public String getName() {
            return "syntax";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return PREFIX + " syntax";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (!Files.exists(getConfigFolder().resolve("bogo.compat.json"))) {
                sender.sendMessage(new TextComponentTranslation("bogosort.command.datadriven.syntax.missing_file", "bogo.compat.json"));
                return;
            }

            try (var reader = Files.newBufferedReader(getConfigFolder().resolve("bogo.compat.json"))) {
                var json = new Gson().fromJson(reader, JsonObject.class);
                for (var element : json.get("actions").getAsJsonArray()) {
                    DataDrivenBogoCompat.SCHEMA_SINGLE.read(element);
                }
                sender.sendMessage(new TextComponentTranslation("bogosort.command.datadriven.syntax.success"));
            } catch (Exception e) {
                sender.sendMessage(new TextComponentTranslation("bogosort.command.datadriven.syntax.error", e.toString()));
            }
        }
    }
}
