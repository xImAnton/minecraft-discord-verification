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
            sender.sendMessage("you don't have the permission to do that!");
            return false;
        }
        sender.sendMessage("Fetching verified players.. ");
        Set<String> players = DiscordVerification.getInstance().getDB().getAllVerifiedPlayers();

        if (players.isEmpty()) {
            sender.sendMessage("there are no verified players");
            return false;
        }

        sender.sendMessage(String.join(", ", players));
        return true;
    }

}
