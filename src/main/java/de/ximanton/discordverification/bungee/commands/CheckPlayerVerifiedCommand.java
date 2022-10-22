package de.ximanton.discordverification.bungee.commands;

import de.ximanton.discordverification.DiscordVerification;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;


public class CheckPlayerVerifiedCommand extends Command {

    public CheckPlayerVerifiedCommand() {
        super("isverified");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("discordverification.check")) {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getNoPermission()));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getPlayerNotSpecified()));
            return;
        }

        sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().formatIsVerifiedResult(args[0], DiscordVerification.getInstance().getDB().isPlayerVerified(args[0]))));
    }
}
