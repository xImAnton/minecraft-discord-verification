package de.ximanton.discordverification.bukkit.commands;

import de.ximanton.discordverification.DiscordVerification;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;


public class CheckPlayerVerifiedCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("discordverification.check")) {
            sender.sendMessage("you don't have the permission to do that!");
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage("please specify the player");
            return false;
        }

        if (DiscordVerification.getInstance().getDB().isPlayerVerified(args[0])) {
            sender.sendMessage(args[0] + " is verified");
        } else {
            sender.sendMessage(args[0] + " is not verified");
        }

        return true;
    }
}
