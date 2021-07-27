package de.ximanton.discordverification;

/**
 * Possible returns from DatabaseConnector#insertPlayer method
 */
public enum InsertPlayerReturn {

    /**
     * Everything is fine and the player has been verified
     */
    OK(),
    /**
     * the player is already verified
     */
    ALREADY_EXISTS(),
    /**
     * the discord user already had another account verified that has been overridden
     */
    OVERRIDDEN(),
    /**
     * an error occurred while talking to the database
     */
    ERROR(),

    /**
     * the verification limit was exceeded
     */
    LIMIT_REACHED()
}
