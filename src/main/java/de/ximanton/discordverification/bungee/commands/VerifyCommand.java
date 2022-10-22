package de.ximanton.discordverification.bungee.commands;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.MojangAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class VerifyCommand extends Command {

    public VerifyCommand() {
        super("verify");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("discordverification.verify")) {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getNoPermission()));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getPlayerNotSpecified()));
            return;
        }
        String uuid = MojangAPI.getPlayerUUID(args[0]);
        if (uuid == null) {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().formatVerifyInvalidPlayer(args[0])));
            return;
        }
        DiscordVerification.getInstance().getDB().insertPlayer(args[0], 0L, true);
        DiscordVerification.getInstance().getDiscord().updateStatus();

        sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().formatVerifySuccess(args[0])));
    }
}
