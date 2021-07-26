package de.ximanton.discordverification;

/**
 * Possible returns from DatabaseConnector#insertPlayer method
 */
public enum InsertPlayerReturn {

    /**
     * Everything is fine and the player has been verified
     */
    OK(":white_check_mark: You have been verified!"),
    /**
     * the player is already verified
     */
    ALREADY_EXISTS(":woman_shrugging: That minecraft account is already verified"),
    /**
     * the discord user already had another account verified that has been overridden
     */
    OVERRIDDEN(":pencil: Your previous verified account has been overridden!"),
    /**
     * an error occurred while talking to the database
     */
    ERROR(":file_folder: There was an error talking to the database"),

    /**
     * the verification limit was exceeded
     */
    LIMIT_REACHED(":chart_with_downwards_trend: The maximal player limit was reached! Too late");

    private final String message;

    InsertPlayerReturn(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
