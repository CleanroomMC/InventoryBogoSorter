package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.SReloadConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

public class ConfigReloadCommand extends CommandBase {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/bogosort reload";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            NetworkHandler.sendToPlayer(new SReloadConfig(), (EntityPlayerMP) sender);
            sender.sendMessage(new TextComponentTranslation("bogosort.command.config_relaod.success"));
        }
    }
}
