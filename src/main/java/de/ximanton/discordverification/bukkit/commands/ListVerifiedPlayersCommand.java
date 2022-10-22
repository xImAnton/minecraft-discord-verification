package de.ximanton.discordverification.bukkit.commands;

import de.ximanton.discordverification.DiscordVerification;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ListVerifiedPlayersCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("discordverification.list")) {
            sender.sendMessage(DiscordVerification.getInstance().getMessages().getNoPermission());
            return false;
        }
        sender.sendMessage(DiscordVerification.getInstance().getMessages().getListVerifiedFetching());
        Set<String> players = DiscordVerification.getInstance().getDB().getAllVerifiedPlayers();

        if (players.isEmpty()) {
            sender.sendMessage(DiscordVerification.getInstance().getMessages().getListVerifiedEmpty());
            return false;
        }

        sender.sendMessage(String.join(", ", players));
        return true;
    }

}
