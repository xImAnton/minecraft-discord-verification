package de.ximanton.discordverification;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinListener implements Listener {

    @EventHandler
    public void onNetworkJoin(PreLoginEvent e) {
        e.registerIntent(DiscordVerification.getInstance());
        DiscordVerification.getInstance().getProxy().getScheduler().runAsync(DiscordVerification.getInstance(), () -> {
            String playerName = e.getConnection().getName();
            // Check if player is verified, if not disconnect him
            if (!DiscordVerification.getInstance().getDB().isPlayerVerified(playerName)) {
                e.setCancelled(true);
                e.setCancelReason(TextComponent.fromLegacyText(DiscordVerification.getInstance().getKickMessage().replace("%name%", playerName)));
                DiscordVerification.getInstance().getProxy().getLogger().warning("Unverified Player " + playerName + " tried to connect to the Server");
            }
            e.completeIntent(DiscordVerification.getInstance());
        });
    }
}
