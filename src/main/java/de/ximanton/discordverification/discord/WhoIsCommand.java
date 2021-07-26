package de.ximanton.discordverification.discord;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.InsertPlayerReturn;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.UserData;

import java.sql.SQLException;

public class WhoIsCommand implements Command {

    @Override
    public void dispatch(Message msg, String[] args) {
        if (args.length < 1) {
            msg.getChannel().block().createMessage(":x: Please specify the IGN of the player who you want to find on Discord");
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

            String returnMsg = user != null ? "`" + ign + "` is " + user.getMention() : ":x: Couldn't find the discord account of `" + ign + "`";
            msg.getChannel().block().createMessage(returnMsg).block();
        } catch (SQLException throwables) {
            msg.getChannel().block().createMessage(InsertPlayerReturn.ERROR.getMessage()).block();
            throwables.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "whois";
    }
}
