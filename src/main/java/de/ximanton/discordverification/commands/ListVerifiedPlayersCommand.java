package de.ximanton.discordverification.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import de.ximanton.discordverification.DiscordVerification;

import java.util.Set;

public class ListVerifiedPlayersCommand extends Command {

    public ListVerifiedPlayersCommand() {
        super("verifiedplayers");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("discordverification.list")) {
            sender.sendMessage(TextComponent.fromLegacyText("you don't have the permission to do that!"));
            return;
        }
        sender.sendMessage(TextComponent.fromLegacyText("Fetching verified players.. "));
        Set<String> players = DiscordVerification.getInstance().getDB().getAllVerifiedPlayers();
        if (players.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText("there are no verified players"));
            return;
        }
        sender.sendMessage(TextComponent.fromLegacyText(String.join(", ", players)));
    }
}
