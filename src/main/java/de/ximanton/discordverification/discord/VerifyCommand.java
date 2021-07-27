package de.ximanton.discordverification.discord;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.InsertPlayerReturn;
import de.ximanton.discordverification.MojangAPI;
import discord4j.core.object.entity.Message;

public class VerifyCommand implements Command {

    @Override
    public void dispatch(Message msg, String[] args) {
        if (args.length < 1) {
            msg.getChannel().block().createMessage(DiscordVerification.getInstance().getMessages().getVerifyNoIgn()).block();
            return;
        }

        String playerName = args[0].toLowerCase();

        // Fetch the uuid of the player to check if it exists
        String uuid = MojangAPI.getPlayerUUID(playerName);

        if (uuid == null) {
            msg.getChannel().block().createMessage(DiscordVerification.getInstance().getMessages().getVerifyInvalidName()).block();
            return;
        }

        if (!msg.getAuthor().isPresent()) {
            msg.getChannel().block().createMessage(DiscordVerification.getInstance().getMessages().getVerifyError()).block();
            return;
        }

        // Try to insert player
        InsertPlayerReturn status = DiscordVerification.getInstance().getDB().insertPlayer(playerName, msg.getAuthor().get().getId().asLong(), false);
        if (status == InsertPlayerReturn.OK) {
            DiscordVerification.getInstance().getDiscord().updateStatus();
        }

        String returnMsg;
        switch (status) {
            case OK -> returnMsg = DiscordVerification.getInstance().getMessages().getVerifyVerified();
            case ALREADY_EXISTS -> returnMsg = DiscordVerification.getInstance().getMessages().getVerifyAlreadyExists();
            case OVERRIDDEN -> returnMsg = DiscordVerification.getInstance().getMessages().getVerifyOverridden();
            case LIMIT_REACHED -> returnMsg = DiscordVerification.getInstance().getMessages().getVerifyLimitReached();
            case ERROR -> returnMsg = DiscordVerification.getInstance().getMessages().getSqlError();
            default -> returnMsg = "unreachable";
        }

        msg.getChannel().block().createMessage(returnMsg).block();
    }

    @Override
    public String getName() {
        return "verify";
    }
}
