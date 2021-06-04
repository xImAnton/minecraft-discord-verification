package de.ximanton.discordverification;

import java.math.BigInteger;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A class to abstact away SQL database calls and connections
 */
public class DatabaseConnector {

    private final Connection connection;

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
            PreparedStatement searchPlayer = connection.prepareStatement("SELECT * FROM verified_users WHERE ign = ?;");
            searchPlayer.setString(1, playerName.toLowerCase());
            // Look for players with the given ign in the db
            ResultSet isVerifiedResult = searchPlayer.executeQuery();
            // Return whether a result has been found or not
            boolean result = isVerifiedResult.next();

            searchPlayer.close();
            isVerifiedResult.close();

            return result;
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
            // Checks if the ign is already verified
            PreparedStatement checkIGNStatement = connection.prepareStatement("SELECT * FROM verified_users WHERE ign = ?;");
            checkIGNStatement.setString(1, playerName.toLowerCase());
            ResultSet checkPlayerResult = checkIGNStatement.executeQuery();

            // if so, return
            if (checkPlayerResult.next()) {
                checkPlayerResult.close();
                checkIGNStatement.close();
                return InsertPlayerReturn.ALREADY_EXISTS;
            }

            checkPlayerResult.close();
            checkIGNStatement.close();

            // Check if the discord user already had an verified account
            PreparedStatement checkDiscordUserAlreadyVerifiedStatement = connection.prepareStatement("SELECT * FROM verified_users WHERE discord = ?;");
            checkDiscordUserAlreadyVerifiedStatement.setInt(1, authorId.intValue());
            ResultSet discordExistsResult = checkDiscordUserAlreadyVerifiedStatement.executeQuery();
            if (discordExistsResult.next()) {
                if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                    DiscordVerification.getInstance().kickPlayer(discordExistsResult.getString("ign"), "Another Player has been verified with your discord account!");
                }
                // Override previous verified ign
                PreparedStatement updateIgnStatement = connection.prepareStatement("UPDATE verified_users SET ign = ? WHERE discord = ?;");
                updateIgnStatement.setString(1, playerName.toLowerCase());
                updateIgnStatement.setInt(2, authorId.intValue());

                updateIgnStatement.executeUpdate();

                discordExistsResult.close();
                updateIgnStatement.close();
                checkDiscordUserAlreadyVerifiedStatement.close();
                discordExistsResult.close();
                return InsertPlayerReturn.OVERRIDDEN;
            }

            discordExistsResult.close();
            checkDiscordUserAlreadyVerifiedStatement.close();


            // If neither the ign already existed nor the discord user had an verified account, insert the ign
            PreparedStatement addUserStatement = connection.prepareStatement("INSERT INTO verified_users(ign, verified, discord) VALUES (?, ?, ?);");
            addUserStatement.setString(1, playerName.toLowerCase());
            addUserStatement.setInt(2, (int) (System.currentTimeMillis() / 1000));
            addUserStatement.setInt(3, authorId.intValue());

            addUserStatement.executeUpdate();
            addUserStatement.close();

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
            // Check the ign of the user to remove to kick him on unverify
            if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                PreparedStatement getDiscordUserIGN = connection.prepareStatement("SELECT * FROM verified_users WHERE discord = ?;");
                getDiscordUserIGN.setInt(1, userId.intValue());

                ResultSet playerIgnResult = getDiscordUserIGN.executeQuery();
                getDiscordUserIGN.close();
                if (playerIgnResult.next()) {
                    playerIgnResult.close();
                    DiscordVerification.getInstance().kickPlayer(playerIgnResult.getString("ign"), "You left the Discord Server!");
                } else {
                    playerIgnResult.close();
                    return;
                }
            }

            // Delete the users record
            PreparedStatement deleteUser = connection.prepareStatement("DELETE FROM verified_users WHERE discord = ?;");

            deleteUser.setInt(1, userId.intValue());
            deleteUser.executeUpdate();
            deleteUser.close();
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
            stmt.close();
            rs.close();
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
            PreparedStatement checkPlayerVerified = connection.prepareStatement("SELECT * FROM verified_users WHERE ign = ?;");
            checkPlayerVerified.setString(1, player.toLowerCase());
            ResultSet rs = checkPlayerVerified.executeQuery();
            checkPlayerVerified.close();

            if (!rs.next()) {
                rs.close();
                return false;
            }
            if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                DiscordVerification.getInstance().kickPlayer(player);
            }

            rs.close();

            PreparedStatement deletePlayer = connection.prepareStatement("DELETE FROM verified_users WHERE ign = ?;");
            deletePlayer.setString(1, player.toLowerCase());

            deletePlayer.executeUpdate();
            deletePlayer.close();
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
