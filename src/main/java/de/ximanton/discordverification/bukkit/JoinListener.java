package de.ximanton.discordverification.bukkit;

import de.ximanton.discordverification.DiscordVerification;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        String playerName = e.getPlayer().getName();

        if (!DiscordVerification.getInstance().getDB().isPlayerVerified(playerName)) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, DiscordVerification.getInstance().getMessages().formatKickMessage(playerName));
            DiscordVerification.getInstance().getPlugin().getLogger().warning("Unverified Player " + playerName + " tried to connect to the Server");
            return;
        }

        e.allow();
    }

}
