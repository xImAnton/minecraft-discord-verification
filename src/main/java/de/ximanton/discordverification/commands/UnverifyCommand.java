package de.ximanton.discordverification.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import de.ximanton.discordverification.DiscordVerification;


public class UnverifyCommand extends Command {

    public UnverifyCommand() {
        super("unverify");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("discordverification.unverify")) {
            sender.sendMessage(TextComponent.fromLegacyText("you don't have the permission to do that!"));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(TextComponent.fromLegacyText("please specify the player"));
            return;
        }
        if (DiscordVerification.getInstance().getDB().unverify(args[0])) {
            sender.sendMessage(TextComponent.fromLegacyText(args[0] + " has been unverified"));
        } else {
            sender.sendMessage(TextComponent.fromLegacyText("couldn't unverify " + args[0]));
        }
    }
}
