package de.ximanton.discordverification.bukkit.commands;

import de.ximanton.discordverification.DiscordVerification;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class UnverifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("discordverification.unverify")) {
            sender.sendMessage("you don't have the permission to do that!");
            return false;
        }
        if (args.length < 1) {
            sender.sendMessage("please specify the player");
            return false;
        }
        if (DiscordVerification.getInstance().getDB().unverify(args[0])) {
            sender.sendMessage(args[0] + " has been unverified");
            DiscordVerification.getInstance().getDiscord().updateStatus();
        } else {
            sender.sendMessage("couldn't unverify " + args[0]);
        }
        return true;
    }
}
