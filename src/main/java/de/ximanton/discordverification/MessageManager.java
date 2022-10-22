package de.ximanton.discordverification;

import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.config.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class MessageManager {

    private final String ignNoUser;
    private final String ignNotVerified;
    private final String ignSuccess;

    private final String verifyNoIgn;
    private final String verifyInvalidName;
    private final String verifyError;
    private final String verifyVerified;
    private final String verifyAlreadyExists;
    private final String verifyOverridden;
    private final String verifyLimitReached;

    private final String whoisNoIgn;
    private final String whoisSuccess;
    private final String whoisNotVerified;

    private final String sqlError;
    private final String status;

    private final String pluginPrefix;
    private final String noPermission;

    private final String playerNotSpecified;
    private final String isVerifiedResult;

    private final String clearSuccess;
    private final String clearConfirm;

    private final String listVerifiedFetching;
    private final String listVerifiedEmpty;

    private final String unverifySuccess;
    private final String unverifyError;

    private final String verifySuccess;
    private final String verifyInvalidPlayer;

    private final String kickUnverify;
    private final String kickNotVerified;
    private final String kickLeftDiscord;
    private final String kickOverridden;

    public MessageManager(Configuration config) {
        ignNoUser = config.getString("ign.no-user", ":x: Please ping the user whose IGN you want to get.");
        kickNotVerified = config.getString("kick.not-verified", "You are not verified!");
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
        pluginPrefix = config.getString("ingame.plugin-prefix", "§7[§bDiscordVerification§7]§8: §r");
        noPermission = config.getString("ingame.no-permission", "§cYou don't have the permission to to this");
        playerNotSpecified = config.getString("ingame.player-not-specified", "Please specify the player!");
        isVerifiedResult = config.getString("ingame.is-verified.result", "$player is §9$result");
        clearSuccess = config.getString("ingame.clear.success", "§aThe verification list has been cleared");
        clearConfirm = config.getString("ingame.clear.confirm", "§cPlease type that again if you really want to delete all verifications. THIS CANNOT BE UNDONE!");
        listVerifiedFetching = config.getString("ingame.list-verifications.fetching", "Fetching verified players..");
        listVerifiedEmpty = config.getString("ingame.list-verifications.empty", "There are no verified players");
        unverifySuccess = config.getString("ingame.unverify.success", "$player has been unverified");
        unverifyError = config.getString("ingame.unverify.error", "Couldn't unverify $player");
        verifyInvalidPlayer = config.getString("ingame.verify.invalid-player", "This player doesn't exist");
        verifySuccess = config.getString("ingame.verify.success", "$player has been verified");
        kickUnverify = config.getString("kick.unverified", "You have been unverified!");
        kickLeftDiscord = config.getString("kick.discord-leave", "You left the Discord Server!");
        kickOverridden = config.getString("kick.overridden", "Another Player has been verified with your discord account!");
    }

    public MessageManager(ConfigurationSection config) {
        ignNoUser = config.getString("ign.no-user", ":x: Please ping the user whose IGN you want to get.");
        kickNotVerified = config.getString("kick.not-verified", "You are not verified!");
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
        pluginPrefix = config.getString("ingame.plugin-prefix", "§7[§bDiscordVerification§7]§8: §r");
        noPermission = config.getString("ingame.no-permission", "§cYou don't have the permission to to this");
        playerNotSpecified = config.getString("ingame.player-not-specified", "Please specify the player!");
        isVerifiedResult = config.getString("ingame.is-verified.result", "$player is §9$result");
        clearSuccess = config.getString("ingame.clear.success", "§aThe verification list has been cleared");
        clearConfirm = config.getString("ingame.clear.confirm", "§cPlease type that again if you really want to delete all verifications. THIS CANNOT BE UNDONE!");
        listVerifiedFetching = config.getString("ingame.list-verifications.fetching", "Fetching verified players..");
        listVerifiedEmpty = config.getString("ingame.list-verifications.empty", "There are no verified players");
        unverifySuccess = config.getString("ingame.unverify.success", "$player has been unverified");
        unverifyError = config.getString("ingame.unverify.error", "Couldn't unverify $player");
        verifyInvalidPlayer = config.getString("ingame.verify.invalid-player", "This player doesn't exist");
        verifySuccess = config.getString("ingame.verify.success", "$player has been verified");
        kickUnverify = config.getString("kick.unverified", "You have been unverified!");
        kickLeftDiscord = config.getString("kick.discord-leave", "You left the Discord Server!");
        kickOverridden = config.getString("kick.overridden", "Another Player has been verified with your discord account!");
    }

    public String formatStatus(int limit, int count) {
        return status.replace("$count", String.valueOf(count)).replace("$limit", String.valueOf(limit));
    }

    public String formatWhoisNotVerified(String ign) {
        return whoisNotVerified.replace("$ign", ign);
    }

    public String formatWhoisSuccess(User user, String ign) {
        return whoisSuccess.replace("$user", user.getAsMention()).replace("$ign", ign);
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
        return ignNotVerified.replace("$user", user.getAsTag());
    }

    public String formatIgnSuccess(User user, String ign) {
        return ignSuccess.replace("$user", user.getAsTag()).replace("$ign", ign);
    }

    public String formatKickNotVerified(String playerName) {
        return kickNotVerified.replace("$player", playerName);
    }

    public String getIgnNoUser() {
        return ignNoUser;
    }

    public String getNoPermission() {
        return pluginPrefix + noPermission;
    }

    public String getPlayerNotSpecified() {
        return pluginPrefix + playerNotSpecified;
    }

    public String formatIsVerifiedResult(String player, boolean isVerified) {
        return pluginPrefix + isVerifiedResult.replace("$player", player).replace("$result", isVerified ? "verified" : "not verified");
    }

    public String getClearSuccess() {
        return pluginPrefix + clearSuccess;
    }

    public String getClearConfirm() {
        return pluginPrefix + clearConfirm;
    }

    public String getListVerifiedFetching() {
        return pluginPrefix + listVerifiedFetching;
    }

    public String getListVerifiedEmpty() {
        return pluginPrefix + listVerifiedEmpty;
    }

    public String formatUnverifySuccess(String player) {
        return pluginPrefix + unverifySuccess.replace("$player", player);
    }

    public String formatUnverifyError(String player) {
        return pluginPrefix + unverifyError.replace("$player", player);
    }

    public String formatVerifySuccess(String player) {
        return pluginPrefix + verifySuccess.replace("$player", player);
    }

    public String formatVerifyInvalidPlayer(String player) {
        return pluginPrefix + verifyInvalidPlayer.replace("$player", player);
    }

    public String getKickUnverify() {
        return kickUnverify;
    }

    public String getKickLeftDiscord() {
        return kickLeftDiscord;
    }

    public String formatKickOverridden(String newPlayer) {
        return kickOverridden.replace("$newPlayer", newPlayer);
    }
}
