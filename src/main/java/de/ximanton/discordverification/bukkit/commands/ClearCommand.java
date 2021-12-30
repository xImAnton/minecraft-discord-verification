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
            sender.sendMessage("you don't have the permission to do that!");
            return false;
        }

        if (System.currentTimeMillis() - lastUse < 10000) {
            DiscordVerification.getInstance().getDB().resetDB();
            sender.sendMessage("the verifications list has been cleared");
            lastUse = 0;
        } else {
            lastUse = System.currentTimeMillis();
            sender.sendMessage("please type that again if you really want to delete all verifications");
        }

        return true;
    }
}
