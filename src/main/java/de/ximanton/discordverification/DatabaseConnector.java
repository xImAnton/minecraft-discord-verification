package de.ximanton.discordverification;

import java.math.BigInteger;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A class to abstract away SQL calls and connections
 */
public class DatabaseConnector {

    private final Connection connection;

    private boolean playerExisting(String ign) throws SQLException {
        PreparedStatement searchPlayer = connection.prepareStatement("SELECT * FROM verified_users WHERE ign = ?;");
        searchPlayer.setString(1, ign.toLowerCase());
        // Look for players with the given ign in the db
        ResultSet isVerifiedResult = searchPlayer.executeQuery();
        boolean res = isVerifiedResult.next();

        isVerifiedResult.close();
        searchPlayer.close();
        return res;
    }

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
            return playerExisting(playerName);
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

    private void updatePlayerIGN(String newIgn, BigInteger discordId) throws SQLException {
        PreparedStatement updateIgnStatement = connection.prepareStatement("UPDATE verified_users SET ign = ? WHERE discord = ?;");
        updateIgnStatement.setString(1, newIgn.toLowerCase());
        updateIgnStatement.setInt(2, discordId.intValue());
        updateIgnStatement.executeUpdate();
        updateIgnStatement.close();
    }

    private Optional<String> isDiscordUserVerified(BigInteger discordId) throws SQLException {
        PreparedStatement checkDiscordUserAlreadyVerifiedStatement = connection.prepareStatement("SELECT * FROM verified_users WHERE discord = ?;");
        checkDiscordUserAlreadyVerifiedStatement.setInt(1, discordId.intValue());
        ResultSet discordExistsResult = checkDiscordUserAlreadyVerifiedStatement.executeQuery();
        boolean res = discordExistsResult.next();

        Optional<String> out;
        if (res) {
            out = Optional.of(discordExistsResult.getString("ign"));
        } else {
            out = Optional.empty();
        }

        discordExistsResult.close();
        checkDiscordUserAlreadyVerifiedStatement.close();

        return out;
    }

    private void addUser(String ign, BigInteger discordId) throws SQLException {
        PreparedStatement addUserStatement = connection.prepareStatement("INSERT INTO verified_users(ign, verified, discord) VALUES (?, ?, ?);");
        addUserStatement.setString(1, ign.toLowerCase());
        addUserStatement.setInt(2, (int) (System.currentTimeMillis() / 1000));
        addUserStatement.setInt(3, discordId.intValue());

        addUserStatement.executeUpdate();
        addUserStatement.close();
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
            // if so, return
            if (playerExisting(playerName)) {
                return InsertPlayerReturn.ALREADY_EXISTS;
            }

            // Check if the discord user already had an verified account
            Optional<String> isUserVerified = isDiscordUserVerified(authorId);
            if (isUserVerified.isPresent()) {
                if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                    DiscordVerification.getInstance().kickPlayer(isUserVerified.get(), "Another Player has been verified with your discord account!");
                }
                // Override previous verified ign
                updatePlayerIGN(playerName, authorId);
                return InsertPlayerReturn.OVERRIDDEN;
            }

            // If neither the ign already existed nor the discord user had an verified account, insert the ign
            addUser(playerName, authorId);

            return InsertPlayerReturn.OK;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return InsertPlayerReturn.ERROR;
    }

    public void deleteUser(BigInteger userId) throws SQLException {
        PreparedStatement deleteUser = connection.prepareStatement("DELETE FROM verified_users WHERE discord = ?;");

        deleteUser.setInt(1, userId.intValue());
        deleteUser.executeUpdate();
        deleteUser.close();
    }

    public void deleteUser(String ign) throws SQLException {
        PreparedStatement deletePlayer = connection.prepareStatement("DELETE FROM verified_users WHERE ign = ?;");
        deletePlayer.setString(1, ign.toLowerCase());

        deletePlayer.executeUpdate();
        deletePlayer.close();
    }

    /**
     * Unverifies a minecraft account by discord user id
     * @param userId The discord user id
     */
    public void removeAccountOfUser(BigInteger userId) {
        try {
            // Check the ign of the user to remove to kick him on unverify
            if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                Optional<String> userIGN = isDiscordUserVerified(userId);
                if (userIGN.isPresent()) {
                    DiscordVerification.getInstance().kickPlayer(userIGN.get(), "You left the Discord Server!");
                } else {
                    return;
                }
            }

            // Delete the users record
            deleteUser(userId);
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
            if (!isPlayerVerified(player)) {
                return false;
            }

            if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                DiscordVerification.getInstance().kickPlayer(player);
            }

            deleteUser(player);

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
