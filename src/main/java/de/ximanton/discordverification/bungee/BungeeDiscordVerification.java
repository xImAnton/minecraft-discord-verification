package de.ximanton.discordverification.bungee;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.IDiscordVerification;
import de.ximanton.discordverification.bungee.commands.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * The Main plugin class
 */
public class BungeeDiscordVerification extends Plugin implements IDiscordVerification {

    private static BungeeDiscordVerification INSTANCE = null;

    public static BungeeDiscordVerification getInstance() {
        return INSTANCE;
    }

    public BungeeDiscordVerification() {
        INSTANCE = this;
    }

    /**
     * Called by bungee on plugin enable
     * Setups the plugin, the database connection and discord and reads the config file
     */
    @Override
    public void onEnable() {
        ensureConfigExisting();

        try {
            DiscordVerification.getInstance().setupBungee(this, getConfig());
        } catch (IOException e) {
            e.printStackTrace();
        }

        PluginManager pluginManager = getProxy().getPluginManager();
        pluginManager.registerCommand(this, new ListVerifiedPlayersCommand());
        pluginManager.registerCommand(this, new CheckPlayerVerifiedCommand());
        pluginManager.registerCommand(this, new UnverifyCommand());
        pluginManager.registerCommand(this, new VerifyCommand());
        pluginManager.registerCommand(this, new ClearCommand());

        pluginManager.registerListener(this, new JoinListener());
    }

    /**
     * Checks if a config file is existing and creates one if not
     */
    private void ensureConfigExisting() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private Configuration getConfig() throws IOException {
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
    }

    /**
     * Called by bungee on plugin disable
     * Closes database and discord connections
     */
    @Override
    public void onDisable() {
        DiscordVerification.getInstance().disable();
    }

    /**
     * Shorthand for kicking players by username from the server
     * @param ign the player ign
     * @param reason the kick message that is shown up on the client
     */
    @Override
    public void kickPlayer(String ign, String reason) {
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            if (player.getName().equalsIgnoreCase(ign)) {
                player.disconnect(TextComponent.fromLegacyText(reason));
            }
        }
    }

}
