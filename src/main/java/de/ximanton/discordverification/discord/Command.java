package de.ximanton.discordverification.discord;

import net.dv8tion.jda.api.entities.Message;

public interface Command {

    void dispatch(Message msg, String[] args);
    String getName();

}
