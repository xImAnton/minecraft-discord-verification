package de.ximanton.discordverification.discord.command;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.InsertPlayerReturn;
import de.ximanton.discordverification.MojangAPI;
import de.ximanton.discordverification.discord.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class VerifyCommand implements Command {

    @Override
    public void dispatch(Message msg, String[] args) {
        if (args.length < 1) {
            msg.getChannel().sendMessage(DiscordVerification.getInstance().getMessages().getVerifyNoIgn()).queue();
            return;
        }

        String playerName = args[0].toLowerCase();

        // Fetch the uuid of the player to check if it exists
        MojangAPI.PlayerResponse playerData = MojangAPI.getPlayerUUIDAndName(playerName);

        if (playerData == null) {
            msg.getChannel().sendMessage(DiscordVerification.getInstance().getMessages().getVerifyInvalidName()).queue();
            return;
        }

        // Try to insert player
        InsertPlayerReturn status = DiscordVerification.getInstance().getDB().insertPlayer(playerName, msg.getAuthor().getIdLong(), false);
        if (status == InsertPlayerReturn.OK) {
            DiscordVerification.getInstance().getDiscord().updateStatus();
        }

        String returnMsg;
        returnMsg = switch (status) {
            case OK -> DiscordVerification.getInstance().getMessages().getVerifyVerified();
            case ALREADY_EXISTS -> DiscordVerification.getInstance().getMessages().getVerifyAlreadyExists();
            case OVERRIDDEN -> DiscordVerification.getInstance().getMessages().getVerifyOverridden();
            case LIMIT_REACHED -> DiscordVerification.getInstance().getMessages().getVerifyLimitReached();
            case ERROR -> DiscordVerification.getInstance().getMessages().getSqlError();
        };

        if ((status == InsertPlayerReturn.OK || status == InsertPlayerReturn.OVERRIDDEN) && DiscordVerification.getInstance().isChangeDiscordName()) {
            Guild g = msg.getGuild();
            User a = msg.getAuthor();

            g.modifyNickname(msg.getMember(), DiscordVerification.getInstance().formatNickName(a.getName(), playerData.name())).queue();
        }

        msg.getChannel().sendMessage(returnMsg).queue();
    }

    @Override
    public String getName() {
        return "verify";
    }
}
