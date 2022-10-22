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
            sender.sendMessage(DiscordVerification.getInstance().getMessages().getNoPermission());
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage(DiscordVerification.getInstance().getMessages().getPlayerNotSpecified());
            return false;
        }

        sender.sendMessage(DiscordVerification.getInstance().getMessages().formatIsVerifiedResult(args[0], DiscordVerification.getInstance().getDB().isPlayerVerified(args[0])));

        return true;
    }
}
