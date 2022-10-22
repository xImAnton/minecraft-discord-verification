package de.ximanton.discordverification.bungee.commands;

import de.ximanton.discordverification.DiscordVerification;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.Set;

public class ListVerifiedPlayersCommand extends Command {

    public ListVerifiedPlayersCommand() {
        super("verifiedplayers");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("discordverification.list")) {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getNoPermission()));
            return;
        }
        sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getListVerifiedFetching()));
        Set<String> players = DiscordVerification.getInstance().getDB().getAllVerifiedPlayers();
        if (players.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getListVerifiedEmpty()));
            return;
        }
        sender.sendMessage(TextComponent.fromLegacyText(String.join(", ", players)));
    }
}
