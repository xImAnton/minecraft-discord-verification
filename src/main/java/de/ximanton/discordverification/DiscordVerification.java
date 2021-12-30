package de.ximanton.discordverification;

import de.ximanton.discordverification.discord.DiscordBot;
import net.md_5.bungee.config.Configuration;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class DiscordVerification {

    private static DiscordVerification INSTANCE = null;

    public static DiscordVerification getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DiscordVerification();
        }
        return INSTANCE;
    }

    private String dbPath;
    private String discordToken;
    private boolean kickPlayersOnUnverify;
    private long guildId;
    private int verificationLimit;
    private MessageManager messages;
    private DatabaseConnector db;
    private DiscordBot discord;
    private IDiscordVerification plugin;

    public void setupBungee(IDiscordVerification plugin, Configuration config) {
        this.plugin = plugin;

        dbPath = config.getString("database-path");
        discordToken = config.getString("discord-token");
        kickPlayersOnUnverify = config.getBoolean("kick-players-on-unverify", true);
        guildId = config.getLong("guild-id");
        verificationLimit = config.getInt("verification-limit", -1);

        messages = new MessageManager(config.getSection("messages"));
        setupCommons();
    }

    public void setupBukkit(IDiscordVerification plugin, FileConfiguration config) {
        this.plugin = plugin;

        dbPath = config.getString("database-path");
        discordToken = config.getString("discord-token");
        kickPlayersOnUnverify = config.getBoolean("kick-players-on-unverify", true);
        guildId = config.getLong("guild-id");
        verificationLimit = config.getInt("verification-limit", -1);

        messages = new MessageManager(Objects.requireNonNull(config.getConfigurationSection("messages")));
        setupCommons();
    }

    private void setupCommons() {
        if (discordToken != null && !discordToken.isEmpty()) {
            discord = new DiscordBot(discordToken);
            discord.start();
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

    public void disable() {
        db.close();
        discord.interrupt();
    }

    public IDiscordVerification getPlugin() {
        return plugin;
    }

    public DatabaseConnector getDB() {
        return db;
    }

    public DiscordBot getDiscord() {
        return discord;
    }

    public MessageManager getMessages() {
        return messages;
    }

    public long getGuildId() {
        return guildId;
    }

    public int getVerificationLimit() {
        return verificationLimit;
    }

    public String getDbPath() {
        return dbPath;
    }

    public boolean isKickPlayersOnUnverify() {
        return kickPlayersOnUnverify;
    }



}
