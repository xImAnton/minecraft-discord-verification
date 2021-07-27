package de.ximanton.discordverification.commands;

import de.ximanton.discordverification.MojangAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import de.ximanton.discordverification.DiscordVerification;

public class VerifyCommand extends Command {

    public VerifyCommand() {
        super("verify");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("discordverification.verify")) {
            sender.sendMessage(TextComponent.fromLegacyText("you don't have the permission to do that!"));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(TextComponent.fromLegacyText("please specify the player"));
            return;
        }
        String uuid = MojangAPI.getPlayerUUID(args[0]);
        if (uuid == null) {
            sender.sendMessage(TextComponent.fromLegacyText("this player doesn't exist"));
            return;
        }
        DiscordVerification.getInstance().getDB().insertPlayer(args[0], 0L, true);
        DiscordVerification.getInstance().getDiscord().updateStatus();

        sender.sendMessage(TextComponent.fromLegacyText(args[0] + " has been verified"));
    }
}
