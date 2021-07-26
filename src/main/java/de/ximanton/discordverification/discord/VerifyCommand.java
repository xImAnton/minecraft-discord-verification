package de.ximanton.discordverification.discord;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.InsertPlayerReturn;
import de.ximanton.discordverification.MojangAPI;
import discord4j.core.object.entity.Message;

public class VerifyCommand implements Command {

    @Override
    public void dispatch(Message msg, String[] args) {
        if (args.length < 1) {
            msg.getChannel().block().createMessage(":x: Please specify your Ingame Name").block();
            return;
        }

        String playerName = args[0].toLowerCase();

        // Fetch the uuid of the player to check if it exists
        String uuid = MojangAPI.getPlayerUUID(playerName);

        if (uuid == null) {
            msg.getChannel().block().createMessage(":x: That minecraft account does not exist").block();
            return;
        }

        if (!msg.getAuthor().isPresent()) {
            msg.getChannel().block().createMessage(":x: There was an error fetching your discord id!").block();
            return;
        }
        // Try to insert player
        InsertPlayerReturn status = DiscordVerification.getInstance().getDB().insertPlayer(playerName, msg.getAuthor().get().getId().asLong(), false);

        msg.getChannel().block().createMessage(status.getMessage()).block();
    }

    @Override
    public String getName() {
        return "verify";
    }
}
