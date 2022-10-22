package de.ximanton.discordverification.discord.command;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.discord.Command;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.sql.SQLException;

public class WhoIsCommand implements Command {

    @Override
    public void dispatch(Message msg, String[] args) {
        if (args.length < 1) {
            msg.getChannel().sendMessage(DiscordVerification.getInstance().getMessages().getWhoisNoIgn()).queue();
            return;
        }

        String ign = args[0].toLowerCase();

        try {
            long userId = DiscordVerification.getInstance().getDB().getUserIdForIGN(ign);
            User user = null;

            if (userId != 0) {
                user = DiscordVerification.getInstance().getDiscord().getClient().getUserById(userId);

                if (user == null) {
                    user = DiscordVerification.getInstance().getDiscord().getClient().retrieveUserById(userId).complete();
                }
            }

            String returnMsg = user != null ? DiscordVerification.getInstance().getMessages().formatWhoisSuccess(user, ign) : DiscordVerification.getInstance().getMessages().formatWhoisNotVerified(ign);
            msg.getChannel().sendMessage(returnMsg).queue();
        } catch (SQLException throwables) {
            msg.getChannel().sendMessage(DiscordVerification.getInstance().getMessages().getSqlError()).queue();
            throwables.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "whois";
    }
}
