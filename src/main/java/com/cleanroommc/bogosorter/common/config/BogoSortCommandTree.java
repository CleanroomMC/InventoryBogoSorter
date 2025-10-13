package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.common.OreDictHelper;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.command.CommandTreeBase;

import org.jetbrains.annotations.NotNull;

public class BogoSortCommandTree extends CommandTreeBase {

    public BogoSortCommandTree() {
        addSubcommand(new ConfigReloadCommand());
        addSubcommand(new CommandBase() {
            @Override
            public String getName() {
                return "hand";
            }

            @Override
            public String getUsage(ICommandSender sender) {
                return "/bogosorter hand";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if (sender instanceof EntityPlayer) {
                    ItemStack itemStack = ((EntityPlayer) sender).getHeldItemMainhand();
                    if (itemStack.isEmpty()) itemStack = ((EntityPlayer) sender).getHeldItemOffhand();
                    if (itemStack.isEmpty()) return;
                    String material = OreDictHelper.getMaterial(itemStack);
                    String prefix = OreDictHelper.getOrePrefix(itemStack);
                    sender.sendMessage(new TextComponentString("Material:  " + material));
                    sender.sendMessage(new TextComponentString("OrePrefix: " + prefix));
                }
            }
        });
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
