package com.cleanroommc.bogosorter.common.config;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class BogoSortCommandTree extends CommandTreeBase {

    @Override
    public @NotNull String getName() {
        return "bogosorter";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "/bogosorter [reload]";
    }

    @Override
    public @NotNull Collection<ICommand> getSubCommands() {
        return Arrays.asList(new ConfigReloadCommand());
    }
}
