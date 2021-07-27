package de.ximanton.discordverification;

import de.ximanton.discordverification.commands.*;
import de.ximanton.discordverification.discord.DiscordBot;
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
import java.math.BigInteger;
import java.nio.file.Files;

/**
 * The Main plugin class
 */
public class DiscordVerification extends Plugin {

    public static DiscordVerification INSTANCE;

    private DatabaseConnector db;
    private String dbPath;
    private String discordToken;
    private DiscordBot discord;
    private boolean kickPlayersOnUnverify;
    private BigInteger guildId;
    private int verificationLimit;
    private MessageManager messages;

    public DiscordVerification() {
        INSTANCE = this;
    }

    public static DiscordVerification getInstance() {
        return INSTANCE;
    }

    /**
     * Called by bungee on plugin enable
     * Setups the plugin, the database connection and discord and reads the config file
     */
    @Override
    public void onEnable() {
        ensureConfigExisting();
        reloadConfig();

        try {
            messages = new MessageManager(getConfig().getSection("messages"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        getProxy().getPluginManager().registerListener(this, new JoinListener());
        registerCommands();
        if (discordToken != null) {
            if (!discordToken.isEmpty()) {
                discord = new DiscordBot(discordToken);
                discord.start();
            }
        }
        boolean createdDB = false;
        File f = new File(dbPath);
        if (!f.exists()) {
            try {
                f.createNewFile();
                createdDB = true;
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        this.db = new DatabaseConnector();
        if (createdDB) db.resetDB();
    }

    /**
     * Registers all plugin commands to the proxy plugin manager
     */
    private void registerCommands() {
        PluginManager pluginManager = getProxy().getPluginManager();
        pluginManager.registerCommand(this, new ListVerifiedPlayersCommand());
        pluginManager.registerCommand(this, new CheckPlayerVerifiedCommand());
        pluginManager.registerCommand(this, new UnverifyCommand());
        pluginManager.registerCommand(this, new VerifyCommand());
        pluginManager.registerCommand(this, new ClearCommand());
    }

    public MessageManager getMessages() {
        return messages;
    }

    public DatabaseConnector getDB() {
        return db;
    }

    public DiscordBot getDiscord() {
        return discord;
    }

    public boolean isKickPlayersOnUnverify() {
        return kickPlayersOnUnverify;
    }


    /**
     * Reads values from the config file and sets the corresponding variables
     */
    private void reloadConfig() {
        try {
            Configuration config = getConfig();
            dbPath = config.getString("database-path");
            discordToken = config.getString("discord-token");
            kickPlayersOnUnverify = config.getBoolean("kick-players-on-unverify");
            guildId = BigInteger.valueOf(config.getLong("guild-id"));
            verificationLimit = config.getInt("verification-limit");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public BigInteger getGuildId() {
        return guildId;
    }


    /**
     * Checks if a config file is existing and creates one if not
     */
    private void ensureConfigExisting() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public String getDbPath() {
        return dbPath;
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
        db.close();
        discord.interrupt();
    }


    /**
     * Shorthand for kicking players by username from the server
     * @param ign the player ign
     * @param reason the kick message that is shown up on the client
     */
    public void kickPlayer(String ign, String reason) {
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            if (player.getName().equalsIgnoreCase(ign)) {
                player.disconnect(TextComponent.fromLegacyText(reason));
            }
        }
    }

    public int getVerificationLimit() {
        return verificationLimit;
    }

    /**
     * Kicks player with default kick message
     * @param ign the player ign
     */
    public void kickPlayer(String ign) {
        kickPlayer(ign, "You have been unverified!");
    }
}
