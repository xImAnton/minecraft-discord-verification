package de.ximanton.discordverification;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.*;


public final class DiscordVerification extends Plugin {

    private static DiscordVerification INSTANCE;
    private String kickMessage;
    private String dbPath;

    public static DiscordVerification getInstance() {
        return INSTANCE;
    }

    public DiscordVerification() {
        DiscordVerification.INSTANCE = this;
    }

    @Override
    public void onEnable() {
        ensureConfigExisting();
        setKickMessage();
        setDBPath();
        getProxy().getPluginManager().registerListener(this, new JoinListener());
    }

    private Connection openConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
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

    private Configuration getConfig() throws IOException {
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
    }

    private void setDBPath() {
        try {
            dbPath = getConfig().getString("database-path");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void setKickMessage() {
        try {
            kickMessage = getConfig().getString("kick-message");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
    }

    public boolean isPlayerVerified(String playerName) {
        getProxy().getLogger().info("Checking player " + playerName);
        try {
            Connection connection = openConnection();
            if (connection == null) {
                return false;
            }
            Statement cmd = connection.createStatement();
            ResultSet rs = cmd.executeQuery("SELECT * FROM verified_users WHERE ign = \"" + playerName.toLowerCase()+ "\"");

            boolean isVerified = rs.next();
            connection.close();

            return isVerified;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
