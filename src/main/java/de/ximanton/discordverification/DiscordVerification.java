package de.ximanton.discordverification;

import de.ximanton.discordverification.commands.*;
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

public class DiscordVerification extends Plugin {

    public static DiscordVerification INSTANCE;

    private DatabaseConnector db;
    private String kickMessage;
    private String dbPath;
    private String discordToken;
    private DiscordBot discord;
    private boolean kickPlayersOnUnverify;
    private BigInteger guildId;

    public DiscordVerification() {
        INSTANCE = this;
    }

    public static DiscordVerification getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        ensureConfigExisting();
        reloadConfig();
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

    private void registerCommands() {
        PluginManager pluginManager = getProxy().getPluginManager();
        pluginManager.registerCommand(this, new ListVerifiedPlayersCommand());
        pluginManager.registerCommand(this, new CheckPlayerVerifiedCommand());
        pluginManager.registerCommand(this, new UnverifyCommand());
        pluginManager.registerCommand(this, new VerifyCommand());
        pluginManager.registerCommand(this, new ClearCommand());
    }

    public DatabaseConnector getDB() {
        return db;
    }

    public boolean isKickPlayersOnUnverify() {
        return kickPlayersOnUnverify;
    }

    private void reloadConfig() {
        try {
            Configuration config = getConfig();
            dbPath = config.getString("database-path");
            kickMessage = config.getString("kick-message");
            discordToken = config.getString("discord-token");
            kickPlayersOnUnverify = config.getBoolean("kick-players-on-unverify");
            guildId = BigInteger.valueOf(config.getLong("guild-id"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public BigInteger getGuildId() {
        return guildId;
    }

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

    public String getKickMessage() {
        return kickMessage;
    }

    public String getDbPath() {
        return dbPath;
    }

    private Configuration getConfig() throws IOException {
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
    }

    @Override
    public void onDisable() {
        db.close();
        discord.interrupt();
    }

    public void kickPlayer(String ign, String reason) {
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            if (player.getName().equalsIgnoreCase(ign)) {
                player.disconnect(TextComponent.fromLegacyText(reason));
            }
        }
    }

    public void kickPlayer(String ign) {
        kickPlayer(ign, "You have been unverified!");
    }
}
