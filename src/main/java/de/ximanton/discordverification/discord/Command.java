package de.ximanton.discordverification.discord;

import discord4j.core.object.entity.Message;

public interface Command {

    void dispatch(Message msg, String[] args);
    String getName();

}
