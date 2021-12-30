package de.ximanton.discordverification.bungee;

import de.ximanton.discordverification.DiscordVerification;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinListener implements Listener {

    @EventHandler
    public void onNetworkJoin(PreLoginEvent e) {
        e.registerIntent(BungeeDiscordVerification.getInstance());
        BungeeDiscordVerification.getInstance().getProxy().getScheduler().runAsync(BungeeDiscordVerification.getInstance(), () -> {
            String playerName = e.getConnection().getName();

            // Check if player is verified, if not disconnect him
            if (!DiscordVerification.getInstance().getDB().isPlayerVerified(playerName)) {
                e.setCancelled(true);
                e.setCancelReason(TextComponent.fromLegacyText(DiscordVerification.getInstance().getMessages().formatKickMessage(playerName)));
                DiscordVerification.getInstance().getPlugin().getLogger().warning("Unverified Player " + playerName + " tried to connect to the Server");
            }
            e.completeIntent(BungeeDiscordVerification.getInstance());
        });
    }

}
