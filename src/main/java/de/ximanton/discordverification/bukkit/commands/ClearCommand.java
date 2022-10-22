package de.ximanton.discordverification.bukkit.commands;

import de.ximanton.discordverification.DiscordVerification;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ClearCommand implements CommandExecutor {

    private long lastUse = 0;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("discordverification.clear")) {
            sender.sendMessage(DiscordVerification.getInstance().getMessages().getNoPermission());
            return false;
        }

        if (System.currentTimeMillis() - lastUse < 10000) {
            DiscordVerification.getInstance().getDB().resetDB();
            sender.sendMessage(DiscordVerification.getInstance().getMessages().getClearSuccess());
            lastUse = 0;
        } else {
            lastUse = System.currentTimeMillis();
            sender.sendMessage(DiscordVerification.getInstance().getMessages().getClearConfirm());
        }

        return true;
    }
}
