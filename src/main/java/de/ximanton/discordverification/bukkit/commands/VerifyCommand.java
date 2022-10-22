package de.ximanton.discordverification.bukkit.commands;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.MojangAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class VerifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("discordverification.verify")) {
            sender.sendMessage(DiscordVerification.getInstance().getMessages().getNoPermission());
            return false;
        }
        if (args.length < 1) {
            sender.sendMessage(DiscordVerification.getInstance().getMessages().getPlayerNotSpecified());
            return false;
        }
        String uuid = MojangAPI.getPlayerUUID(args[0]);
        if (uuid == null) {
            sender.sendMessage(DiscordVerification.getInstance().getMessages().formatVerifyInvalidPlayer(args[0]));
            return false;
        }
        DiscordVerification.getInstance().getDB().insertPlayer(args[0], 0L, true);
        DiscordVerification.getInstance().getDiscord().updateStatus();

        sender.sendMessage(DiscordVerification.getInstance().getMessages().formatVerifySuccess(args[0]));
        return true;
    }

}
