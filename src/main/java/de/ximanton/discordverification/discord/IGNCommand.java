package de.ximanton.discordverification.discord;

import de.ximanton.discordverification.DiscordVerification;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

import java.sql.SQLException;
import java.util.Optional;

public class IGNCommand implements Command {

    @Override
    public void dispatch(Message msg, String[] args) {
        User user = args.length < 1 ? msg.getAuthor().orElse(null) : msg.getUserMentions().blockFirst();

        if (user == null) {
            msg.getChannel().block().createMessage(DiscordVerification.getInstance().getMessages().getIgnNoUser()).block();
            return;
        }

        try {

            Optional<String> ign = DiscordVerification.getInstance().getDB().getUserIGN(user.getId().asLong());

            String returnMsg = ign.map(s -> DiscordVerification.getInstance().getMessages().formatIgnSuccess(user, s)).orElseGet(() -> DiscordVerification.getInstance().getMessages().formatIgnNotVerified(user));
            msg.getChannel().block().createMessage(returnMsg).block();

        } catch (SQLException throwables) {

            msg.getChannel().block().createMessage(DiscordVerification.getInstance().getMessages().getSqlError()).block();
            throwables.printStackTrace();

        }

    }

    @Override
    public String getName() {
        return "ign";
    }
}
