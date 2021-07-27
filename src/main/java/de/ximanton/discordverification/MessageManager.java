package de.ximanton.discordverification;

import discord4j.core.object.entity.User;
import net.md_5.bungee.config.Configuration;

public class MessageManager {

    private final String ignNoUser;
    private final String kickMessage;
    private final String ignSuccess;
    private final String ignNotVerified;
    private final String verifyNoIgn;
    private final String verifyInvalidName;
    private final String verifyError;
    private final String verifyVerified;
    private final String verifyAlreadyExists;
    private final String verifyOverridden;
    private final String verifyLimitReached;
    private final String sqlError;
    private final String whoisNoIgn;
    private final String whoisSuccess;
    private final String whoisNotVerified;
    private final String status;

    public MessageManager(Configuration config) {
        ignNoUser = config.getString("ign.no-user", ":x: Please ping the user whose IGN you want to get.");
        kickMessage = config.getString("not-verified", "You are not verified!");
        ignSuccess = config.getString("ign.success", "The IGN of `$user` is `$ign`");
        ignNotVerified = config.getString("ign.not-verified", "`$user` hasn't verified yet");
        verifyNoIgn = config.getString("verify.no-ign", ":x: Please specify your Ingame Name");
        verifyInvalidName = config.getString("verify.invalid-name", ":x: That minecraft account does not exist");
        verifyError = config.getString("verify.id-error", ":x: There was an error fetching your discord id!");
        verifyVerified = config.getString("verify.verified", ":white_check_mark: You have been verified!");
        verifyAlreadyExists = config.getString("verify.already-existing", ":woman_shrugging: That minecraft account is already verified");
        verifyOverridden = config.getString("verify.overridden", ":pencil: Your previous verified account has been overridden!");
        verifyLimitReached = config.getString("verify.limit-reached", ":chart_with_downwards_trend: The maximal player limit was reached! Too late");
        sqlError = config.getString("sql-error", ":file_folder: There was an error talking to the database");
        whoisNoIgn = config.getString("whois.no-ign", ":x: Please specify the IGN of the player who you want to find on Discord");
        whoisSuccess = config.getString("whois.success", "`$ign` is $user");
        whoisNotVerified = config.getString("whois.not-verified", ":x: Couldn't find the discord account of `$ign`");
        status = config.getString("status", "$count/$limit players verified");
    }

    public String formatStatus(int limit, int count) {
        return status.replace("$count", String.valueOf(count)).replace("$limit", String.valueOf(limit));
    }

    public String formatWhoisNotVerified(String ign) {
        return whoisNotVerified.replace("$ign", ign);
    }

    public String formatWhoisSuccess(User user, String ign) {
        return whoisSuccess.replace("$user", user.getMention()).replace("$ign", ign);
    }

    public String getWhoisNoIgn() {
        return whoisNoIgn;
    }

    public String getSqlError() {
        return sqlError;
    }

    public String getVerifyLimitReached() {
        return verifyLimitReached;
    }

    public String getVerifyOverridden() {
        return verifyOverridden;
    }

    public String getVerifyAlreadyExists() {
        return verifyAlreadyExists;
    }

    public String getVerifyVerified() {
        return verifyVerified;
    }

    public String getVerifyError() {
        return verifyError;
    }

    public String getVerifyInvalidName() {
        return verifyInvalidName;
    }

    public String getVerifyNoIgn() {
        return verifyNoIgn;
    }

    public String formatIgnNotVerified(User user) {
        return ignNotVerified.replace("$user", user.getTag());
    }

    public String formatIgnSuccess(User user, String ign) {
        return ignSuccess.replace("$user", user.getTag()).replace("$ign", ign);
    }

    public String formatKickMessage(String playerName) {
        return kickMessage.replace("%name%", playerName);
    }

    public String getIgnNoUser() {
        return ignNoUser;
    }

}
