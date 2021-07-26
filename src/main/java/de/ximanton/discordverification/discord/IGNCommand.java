package de.ximanton.discordverification.discord;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.InsertPlayerReturn;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

import java.sql.SQLException;
import java.util.Optional;

public class IGNCommand implements Command {

    @Override
    public void dispatch(Message msg, String[] args) {
        User user = args.length < 1 ? msg.getAuthor().orElse(null) : msg.getUserMentions().blockFirst();

        if (user == null) {
            msg.getChannel().block().createMessage(":x: Please ping the user whose IGN you want to get.").block();
            return;
        }

        try {
            Optional<String> ign = DiscordVerification.getInstance().getDB().getUserIGN(user.getId().asLong());

            String returnMsg = ign.map(s -> "The IGN of `" + user.getTag() + "` is `" + s + "`").orElseGet(() -> "`" + user.getTag() + "` hasn't verified yet");
            msg.getChannel().block().createMessage(returnMsg).block();
        } catch (SQLException throwables) {
            msg.getChannel().block().createMessage(InsertPlayerReturn.ERROR.getMessage()).block();
            throwables.printStackTrace();
        }

    }

    @Override
    public String getName() {
        return "ign";
    }
}
