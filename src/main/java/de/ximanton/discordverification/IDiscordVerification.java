package de.ximanton.discordverification;

import java.util.logging.Logger;

public interface IDiscordVerification {

    void kickPlayer(String ign, String reason);
    Logger getLogger();

}
