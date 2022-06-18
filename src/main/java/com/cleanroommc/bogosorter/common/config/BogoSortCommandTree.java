package com.cleanroommc.bogosorter.common.config;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;
import org.jetbrains.annotations.NotNull;

public class BogoSortCommandTree extends CommandTreeBase {

    public BogoSortCommandTree() {
        addSubcommand(new ConfigReloadCommand());
    }

    @Override
    public @NotNull String getName() {
        return "bogosorter";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "/bogosorter [reload]";
    }
}
