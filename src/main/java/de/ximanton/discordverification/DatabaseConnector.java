package de.ximanton.discordverification;

import java.math.BigInteger;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A class to abstact away SQL database calls and connections
 */
public class DatabaseConnector {

    private Connection connection;

    /**
     * Checks if a player is verified
     * @param playerName The player ign to check
     * @return true if the player is verified, false if not or there was an error
     */
    public boolean isPlayerVerified(String playerName) {
        DiscordVerification.getInstance().getProxy().getLogger().info("Checking player " + playerName);
        try {
            if (connection == null) {
                return false;
            }
            Statement cmd = connection.createStatement();
            // Look for players with the given ign in the db
            ResultSet rs = cmd.executeQuery("SELECT * FROM verified_users WHERE ign = \"" + playerName.toLowerCase()+ "\"");
            // Return whether a result has been found or not
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
            // Initialise the SQLite driver
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

    /**
     * Creates a new verification
     * @param playerName The player ign to verify
     * @param authorId The discord user id to assign
     * @return The status of the insertion
     */
    public InsertPlayerReturn insertPlayer(String playerName, BigInteger authorId) {
        try {
            Statement stmt = connection.createStatement();
            // Checks if the ign is already verified
            ResultSet rs = stmt.executeQuery("SELECT * FROM verified_users WHERE ign = \"" + playerName.toLowerCase() + "\";");
            // if so, return
            if (rs.next()) {
                rs.close();
                stmt.close();
                return InsertPlayerReturn.ALREADY_EXISTS;
            }
            // Check if the discord user already had an verified account
            rs = stmt.executeQuery("SELECT * FROM verified_users WHERE discord = " + authorId.toString() + ";");
            if (rs.next()) {
                if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                    DiscordVerification.getInstance().kickPlayer(rs.getString("ign"), "Another Player has been verified with your discord account!");
                }
                // Override previous verified ign
                stmt.executeUpdate("DELETE FROM verified_users WHERE discord = " + authorId.toString() + ";");
                stmt.executeUpdate("INSERT INTO verified_users(ign, verified, discord) VALUES (\"" + playerName.toLowerCase() + "\", " + System.currentTimeMillis() / 1000 + ", " + authorId + ");");
                rs.close();
                stmt.close();
                return InsertPlayerReturn.OVERRIDDEN;
            }
            // If neither the ign already existed nor the discord user had an verified account, insert the ign
            stmt.executeUpdate("INSERT INTO verified_users(ign, verified, discord) VALUES (\"" + playerName.toLowerCase() + "\", " + System.currentTimeMillis() / 1000 + ", " + authorId + ");");
            rs.close();
            stmt.close();
            return InsertPlayerReturn.OK;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return InsertPlayerReturn.ERROR;
    }

    /**
     * Unverifies a minecraft account by discord user id
     * @param userId The discord user id
     */
    public void removeAccountOfUser(BigInteger userId) {
        try {
            Statement stmt = connection.createStatement();
            // Check the ign of the user to remove to kick him on unverify
            if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM verified_users WHERE discord = " + userId.toString() + ";");
                if (rs.next()) {
                    DiscordVerification.getInstance().kickPlayer(rs.getString("ign"), "You left the Discord Server!");
                }
            }
            // Delete the users record
            stmt.executeUpdate("DELETE FROM verified_users WHERE discord = " + userId + ";");
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    /**
     * Fetches all verified players
     * @return A set of player igns as Strings
     */
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

    /**
     * Unverifies a player by ign
     * @param player the players ign
     * @return true if the player was unverified successful, false when an error occurred or the player wasn't verified
     */
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

    /**
     * Deletes and recreates the verified_users table. We could just delete all entry from this table but we do it this way
     * to setup a new DB with the same method
     */
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
