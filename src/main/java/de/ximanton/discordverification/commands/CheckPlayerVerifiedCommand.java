package de.ximanton.discordverification.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import de.ximanton.discordverification.DiscordVerification;


public class CheckPlayerVerifiedCommand extends Command {

    public CheckPlayerVerifiedCommand() {
        super("isverified");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("discordverification.check")) {
            sender.sendMessage(TextComponent.fromLegacyText("you don't have the permission to do that!"));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(TextComponent.fromLegacyText("please specify the player"));
            return;
        }
        if (DiscordVerification.getInstance().getDB().isPlayerVerified(args[0])) {
            sender.sendMessage(TextComponent.fromLegacyText(args[0] + " is verified"));
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(args[0] + " is not verified"));
        }
    }
}
