package de.ximanton.discordverification;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

public class DiscordBot extends Thread {

    private final DiscordClient client;
    private GatewayDiscordClient gateway;

    public DiscordBot(String token) {
        this.client = DiscordClient.create(token);
    }

    public void onMessage(MessageCreateEvent e) {
        final Message msg = e.getMessage();
        if (!msg.getGuild().block().getId().asBigInteger().equals(DiscordVerification.getInstance().getGuildId())) return;
        if (msg.getContent().startsWith("!verify ")) {
            String playerName = msg.getContent().substring(8);
            String uuid = MojangAPI.getPlayerUUID(playerName);
            if (uuid != null) {
                if (!msg.getAuthor().isPresent()) {
                    msg.getChannel().block().createMessage(":x: There was an error fetching your discord id!").block();
                    return;
                }
                InsertPlayerReturn status = DiscordVerification.getInstance().getDB().insertPlayer(playerName, msg.getAuthor().get().getId().asBigInteger());
                String returnMsg;
                switch (status) {
                    case ALREADY_EXISTS:
                        returnMsg = ":x: That minecraft account is already verified";
                        break;
                    case OK:
                        returnMsg = ":white_check_mark: You have been verified!";
                        break;
                    case OVERRIDDEN:
                        returnMsg = ":pencil: Your previous verified account has been overridden!";
                        break;
                    case ERROR:
                        returnMsg = ":file_folder: There was an error talking to the database";
                        break;
                    default:
                        returnMsg = ":x: There was an error";
                }
                msg.getChannel().block().createMessage(returnMsg).block();
            } else {
                msg.getChannel().block().createMessage(":x: That minecraft account does not exist").block();
            }
        }
    }

    public void onMemberLeave(MemberLeaveEvent e) {
        if (!e.getGuild().block().getId().asBigInteger().equals(DiscordVerification.getInstance().getGuildId())) return;
        DiscordVerification.getInstance().getDB().removeAccountOfUser(e.getUser().getId().asBigInteger());
    }

    @Override
    public void run() {
        this.gateway = client.login().block();
        gateway.on(MessageCreateEvent.class).subscribe(this::onMessage);
        gateway.on(ReadyEvent.class).subscribe(e -> DiscordVerification.getInstance().getProxy().getLogger().info("Discord is ready!"));
        gateway.on(MemberLeaveEvent.class).subscribe(this::onMemberLeave);
    }

    @Override
    public void interrupt() {
        gateway.logout();
        DiscordVerification.getInstance().getProxy().getLogger().info("Disconnecting Discord");
        super.interrupt();
    }
}
