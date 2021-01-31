package de.ximanton.discordverification;

import java.math.BigInteger;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DatabaseConnector {

    private Connection connection;

    public boolean isPlayerVerified(String playerName) {
        DiscordVerification.getInstance().getProxy().getLogger().info("Checking player " + playerName);
        try {
            if (connection == null) {
                return false;
            }
            Statement cmd = connection.createStatement();
            ResultSet rs = cmd.executeQuery("SELECT * FROM verified_users WHERE ign = \"" + playerName.toLowerCase()+ "\"");
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public DatabaseConnector() {
        this.connection = openConnection();
        if (connection == null) {
            throw new RuntimeException("database connection couldn't be established");
        }
        DiscordVerification.getInstance().getProxy().getLogger().info("database connection established");
    }

    private Connection openConnection() {
        DiscordVerification.getInstance().getProxy().getLogger().info("establishing database connection");
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + DiscordVerification.getInstance().getDbPath());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            connection.close();
            DiscordVerification.getInstance().getProxy().getLogger().info("database connection closed");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public InsertPlayerReturn insertPlayer(String playerName, BigInteger authorId) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM verified_users WHERE ign = \"" + playerName.toLowerCase() + "\";");
            if (rs.next()) {
                rs.close();
                stmt.close();
                return InsertPlayerReturn.ALREADY_EXISTS;
            }
            rs = stmt.executeQuery("SELECT * FROM verified_users WHERE discord = " + authorId.toString() + ";");
            if (rs.next()) {
                if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                    DiscordVerification.getInstance().kickPlayer(rs.getString("ign"), "Another Player has been verified with your discord account!");
                }
                stmt.executeUpdate("DELETE FROM verified_users WHERE discord = " + authorId.toString() + ";");
                stmt.executeUpdate("INSERT INTO verified_users(ign, verified, discord) VALUES (\"" + playerName.toLowerCase() + "\", " + System.currentTimeMillis() / 1000 + ", " + authorId + ");");
                rs.close();
                stmt.close();
                return InsertPlayerReturn.OVERRIDDEN;
            }
            stmt.executeUpdate("INSERT INTO verified_users(ign, verified, discord) VALUES (\"" + playerName.toLowerCase() + "\", " + System.currentTimeMillis() / 1000 + ", " + authorId + ");");
            rs.close();
            stmt.close();
            return InsertPlayerReturn.OK;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return InsertPlayerReturn.ERROR;
    }

    public void removeAccountOfUser(BigInteger userId) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM verified_users WHERE discord = " + userId.toString() + ";");
            if (rs.next()) {
                if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                    DiscordVerification.getInstance().kickPlayer(rs.getString("ign"), "You left the Discord Server!");
                }
            }
            stmt.executeUpdate("DELETE FROM verified_users WHERE discord = " + userId + ";");
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public Set<String> getAllVerifiedPlayers() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM verified_users");
            HashSet<String> players = new HashSet<>();
            while (rs.next()) {
                players.add(rs.getString("ign"));
            }
            return players;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public boolean unverify(String player) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM verified_users WHERE ign = \"" + player.toLowerCase() + "\";");
            if (!rs.next()) return false;
            if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                DiscordVerification.getInstance().kickPlayer(player, "You have been unverified!");
            }
            stmt.executeUpdate("DELETE FROM verified_users WHERE ign = \"" + player.toLowerCase() + "\";");
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public void resetDB() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE IF EXISTS verified_users");
            statement.executeUpdate("CREATE TABLE verified_users (id INTEGER PRIMARY KEY, ign TEXT, verified INTEGER, discord INTEGER);");
            statement.closeOnCompletion();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
