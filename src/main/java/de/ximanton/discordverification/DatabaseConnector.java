package de.ximanton.discordverification;

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
     *
     * @param playerName The player ign to check
     * @return true if the player is verified, false if not or there was an error
     */
    public boolean isPlayerVerified(String playerName) {
        DiscordVerification.getInstance().getPlugin().getLogger().info("Checking player " + playerName);
        try {
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
        DiscordVerification.getInstance().getPlugin().getLogger().info("database connection established");
    }

    private Connection openConnection() {
        DiscordVerification.getInstance().getPlugin().getLogger().info("establishing database connection");
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
            DiscordVerification.getInstance().getPlugin().getLogger().info("database connection closed");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void updatePlayerIGN(String newIgn, long discordId) throws SQLException {
        PreparedStatement updateIgnStatement = connection.prepareStatement("UPDATE verified_users SET ign = ? WHERE discord = ?;");
        updateIgnStatement.setString(1, newIgn.toLowerCase());
        updateIgnStatement.setLong(2, discordId);
        updateIgnStatement.executeUpdate();
        updateIgnStatement.close();
    }

    public Optional<String> getUserIGN(long discordId) throws SQLException {
        PreparedStatement checkDiscordUserAlreadyVerifiedStatement = connection.prepareStatement("SELECT * FROM verified_users WHERE discord = ?;");
        checkDiscordUserAlreadyVerifiedStatement.setLong(1, discordId);
        ResultSet discordExistsResult = checkDiscordUserAlreadyVerifiedStatement.executeQuery();

        Optional<String> out = discordExistsResult.next() ? Optional.of(discordExistsResult.getString("ign")) : Optional.empty();

        discordExistsResult.close();
        checkDiscordUserAlreadyVerifiedStatement.close();

        return out;
    }

    private void addUser(String ign, long discordId) throws SQLException {
        PreparedStatement addUserStatement = connection.prepareStatement("INSERT INTO verified_users (ign, verified, discord) VALUES (?, ?, ?);");
        addUserStatement.setString(1, ign.toLowerCase());
        addUserStatement.setInt(2, (int) (System.currentTimeMillis() / 1000));
        addUserStatement.setLong(3, discordId);

        addUserStatement.executeUpdate();
        addUserStatement.close();
    }

    public int getVerificationCount() throws SQLException {
        Statement countStatement = connection.createStatement();
        ResultSet results = countStatement.executeQuery("SELECT COUNT(*) FROM verified_users");

        int out = results.getInt(1);

        countStatement.close();
        results.close();

        return out;
    }

    public long getUserIdForIGN(String ign) throws SQLException {
        PreparedStatement getUserStatement = connection.prepareStatement("SELECT discord FROM verified_users WHERE ign = ?;");
        getUserStatement.setString(1, ign);
        ResultSet results = getUserStatement.executeQuery();

        long userId = results.next() ? results.getLong(1) : 0;

        getUserStatement.close();
        results.close();

        return userId;
    }

    /**
     * Creates a new verification
     *
     * @param playerName The player ign to verify
     * @param authorId   The discord user id to assign
     * @return The status of the insertion
     */
    public InsertPlayerReturn insertPlayer(String playerName, long authorId, boolean ignoreLimit) {
        try {
            // Checks if the ign is already verified
            // if so, return
            if (playerExisting(playerName)) {
                return InsertPlayerReturn.ALREADY_EXISTS;
            }

            // Check if the discord user already had a verified account
            Optional<String> isUserVerified = getUserIGN(authorId);
            if (isUserVerified.isPresent()) {
                if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                    DiscordVerification.getInstance().getPlugin().kickPlayer(isUserVerified.get(), "Another Player has been verified with your discord account!");
                }
                // Override previous verified ign
                updatePlayerIGN(playerName, authorId);
                return InsertPlayerReturn.OVERRIDDEN;
            }

            if ((DiscordVerification.getInstance().getVerificationLimit() > 0 && DiscordVerification.getInstance().getVerificationLimit() <= getVerificationCount()) && !ignoreLimit) {
                return InsertPlayerReturn.LIMIT_REACHED;
            }

            DiscordVerification.getInstance().getDiscord().performRoleUpdate(authorId, true);

            // If neither the ign already existed nor the discord user had an verified account, insert the ign
            addUser(playerName, authorId);

            return InsertPlayerReturn.OK;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return InsertPlayerReturn.ERROR;
    }

    public void deleteUser(long userId) throws SQLException {
        PreparedStatement deleteUser = connection.prepareStatement("DELETE FROM verified_users WHERE discord = ?;");

        deleteUser.setLong(1, userId);
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
     *
     * @param userId The discord user id
     */
    public void removeAccountOfUser(long userId) {
        try {
            // Check the ign of the user to remove to kick them on unverify
            if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                Optional<String> userIGN = getUserIGN(userId);
                if (userIGN.isPresent()) {
                    DiscordVerification.getInstance().getPlugin().kickPlayer(userIGN.get(), "You left the Discord Server!");
                } else {
                    // no ign for discord user that left was found, ignoring
                    return;
                }
            }

            // we don't have to add/remove roles when the user left
            // DiscordVerification.getInstance().getDiscord().performRoleUpdate(userId, false);

            // Delete the users record
            deleteUser(userId);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    /**
     * Fetches all verified players
     *
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
     *
     * @param player the players ign
     * @return true if the player was unverified successful, false when an error occurred or the player wasn't verified
     */
    public boolean unverify(String player) {
        try {
            if (!isPlayerVerified(player)) {
                return false;
            }

            if (DiscordVerification.getInstance().isKickPlayersOnUnverify()) {
                DiscordVerification.getInstance().getPlugin().kickPlayer(player, "you have been unverified"); // TODO: add message to config
            }

            DiscordVerification.getInstance().getDiscord().performRoleUpdate(player, false);

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
            statement.executeUpdate("CREATE TABLE verified_users (id INTEGER PRIMARY KEY, ign TEXT, verified INTEGER, discord LONG);");
            statement.closeOnCompletion();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
