package de.ximanton.discordverification.discord;

import de.ximanton.discordverification.DiscordVerification;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.UserData;

import java.sql.SQLException;

public class WhoIsCommand implements Command {

    @Override
    public void dispatch(Message msg, String[] args) {
        if (args.length < 1) {
            msg.getChannel().block().createMessage(DiscordVerification.getInstance().getMessages().getWhoisNoIgn()).block();
            return;
        }

        String ign = args[0].toLowerCase();

        try {
            long userId = DiscordVerification.getInstance().getDB().getUserIdForIGN(ign);
            User user = null;

            if (userId != 0) {
                UserData data = DiscordVerification.getInstance().getDiscord().getClient().getUserById(Snowflake.of(userId)).getData().block();

                if (data != null) {
                    user = new User(DiscordVerification.getInstance().getDiscord().getGateway(), data);
                }
            }

            String returnMsg = user != null ? DiscordVerification.getInstance().getMessages().formatWhoisSuccess(user, ign) : DiscordVerification.getInstance().getMessages().formatWhoisNotVerified(ign);
            msg.getChannel().block().createMessage(returnMsg).block();
        } catch (SQLException throwables) {
            msg.getChannel().block().createMessage(DiscordVerification.getInstance().getMessages().getSqlError()).block();
            throwables.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "whois";
    }
}
