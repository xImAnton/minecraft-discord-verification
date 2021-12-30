package de.ximanton.discordverification.discord.command;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.discord.Command;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.sql.SQLException;
import java.util.Optional;

public class IGNCommand implements Command {

    @Override
    public void dispatch(Message msg, String[] args) {
        User user = args.length < 1 ? msg.getAuthor() : msg.getMentionedUsers().stream().findFirst().orElse(null);

        if (user == null) {
            msg.getChannel().sendMessage(DiscordVerification.getInstance().getMessages().getIgnNoUser()).queue();
            return;
        }

        try {
            Optional<String> ign = DiscordVerification.getInstance().getDB().getUserIGN(user.getIdLong());

            String returnMsg = ign.map(s -> DiscordVerification.getInstance().getMessages().formatIgnSuccess(user, s)).orElseGet(() -> DiscordVerification.getInstance().getMessages().formatIgnNotVerified(user));
            msg.getChannel().sendMessage(returnMsg).queue();
        } catch (SQLException throwables) {
            msg.getChannel().sendMessage(DiscordVerification.getInstance().getMessages().getSqlError()).queue();
            throwables.printStackTrace();
        }

    }

    @Override
    public String getName() {
        return "ign";
    }

}
