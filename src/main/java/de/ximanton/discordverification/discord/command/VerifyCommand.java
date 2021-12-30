package de.ximanton.discordverification.discord.command;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.InsertPlayerReturn;
import de.ximanton.discordverification.MojangAPI;
import de.ximanton.discordverification.discord.Command;
import net.dv8tion.jda.api.entities.Message;

public class VerifyCommand implements Command {

    @Override
    public void dispatch(Message msg, String[] args) {
        if (args.length < 1) {
            msg.getChannel().sendMessage(DiscordVerification.getInstance().getMessages().getVerifyNoIgn()).queue();
            return;
        }

        String playerName = args[0].toLowerCase();

        // Fetch the uuid of the player to check if it exists
        String uuid = MojangAPI.getPlayerUUID(playerName);

        if (uuid == null) {
            msg.getChannel().sendMessage(DiscordVerification.getInstance().getMessages().getVerifyInvalidName()).queue();
            return;
        }

        // Try to insert player
        InsertPlayerReturn status = DiscordVerification.getInstance().getDB().insertPlayer(playerName, msg.getAuthor().getIdLong(), false);
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

        msg.getChannel().sendMessage(returnMsg).queue();
    }

    @Override
    public String getName() {
        return "verify";
    }
}
