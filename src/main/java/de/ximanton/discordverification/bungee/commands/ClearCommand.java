package de.ximanton.discordverification.bungee.commands;

import de.ximanton.discordverification.DiscordVerification;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class ClearCommand extends Command {

    private long lastUse = 0;

    public ClearCommand() {
        super("clearverifications");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("discordverification.clear")) {
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getNoPermission()));
            return;
        }
        if (System.currentTimeMillis() - lastUse < 10000) {
            DiscordVerification.getInstance().getDB().resetDB();
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getClearSuccess()));
            lastUse = 0;
        } else {
            lastUse = System.currentTimeMillis();
            sender.sendMessage(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().getClearConfirm()));
        }
    }
}
