package de.ximanton.discordverification.bungee.commands;

import de.ximanton.discordverification.DiscordVerification;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;


public class UnverifyCommand extends Command {

    public UnverifyCommand() {
        super("unverify");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("discordverification.unverify")) {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getNoPermission()));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getPlayerNotSpecified()));
            return;
        }
        if (DiscordVerification.getInstance().getDB().unverify(args[0])) {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().formatUnverifySuccess(args[0])));
            DiscordVerification.getInstance().getDiscord().updateStatus();
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().formatUnverifyError(args[0])));
        }
    }
}
