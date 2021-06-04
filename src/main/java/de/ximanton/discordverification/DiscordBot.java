package de.ximanton.discordverification;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;


/**
 * The class to manage the discord bot user.
 * This is a Thread to not block the entire Proxy while listening for discord events
 */
public class DiscordBot extends Thread {

    private final DiscordClient client;
    private GatewayDiscordClient gateway;

    /**
     * @param token The discord token to connect to the gateway
     */
    public DiscordBot(String token) {
        this.client = DiscordClient.create(token);
    }

    /**
     * Called when a new discord message is received
     * @param e the MessageCreateEvent
     */
    public void onMessage(MessageCreateEvent e) {
        final Message msg = e.getMessage();
        // if the message is on the wrong guild, do nothing
        if (!msg.getGuild().block().getId().asBigInteger().equals(DiscordVerification.getInstance().getGuildId())) return;
        // Check if message is verify command
        if (msg.getContent().startsWith("!verify ")) {
            String playerName = msg.getContent().substring(8);
            // Fetch the uuid of the player to check if it exists
            String uuid = MojangAPI.getPlayerUUID(playerName);
            if (uuid != null) {
                if (!msg.getAuthor().isPresent()) {
                    msg.getChannel().block().createMessage(":x: There was an error fetching your discord id!").block();
                    return;
                }
                // Try to insert player
                InsertPlayerReturn status = DiscordVerification.getInstance().getDB().insertPlayer(playerName, msg.getAuthor().get().getId().asBigInteger());
                String returnMsg;
                switch (status) {
                    case ALREADY_EXISTS:
                        returnMsg = ":woman_shrugging: That minecraft account is already verified";
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
                        returnMsg = ":x: An error occurred";
                }
                msg.getChannel().block().createMessage(returnMsg).block();
            } else {
                msg.getChannel().block().createMessage(":x: That minecraft account does not exist").block();
            }
        }
    }

    /**
     * Removes the account of a user if he leaves the guild
     * @param e the MemberLeaveEvent
     */
    public void onMemberLeave(MemberLeaveEvent e) {
        if (!e.getGuild().block().getId().asBigInteger().equals(DiscordVerification.getInstance().getGuildId())) return;
        DiscordVerification.getInstance().getDB().removeAccountOfUser(e.getUser().getId().asBigInteger());
    }

    /**
     * The overridden Thread#run
     */
    @Override
    public void run() {
        // login to discord
        this.gateway = client.login().block();
        // Register events
        assert gateway != null;
        gateway.on(MessageCreateEvent.class).subscribe(this::onMessage);
        gateway.on(ReadyEvent.class).subscribe(e -> DiscordVerification.getInstance().getProxy().getLogger().info("Discord is ready!"));
        gateway.on(MemberLeaveEvent.class).subscribe(this::onMemberLeave);
    }

    /**
     * Disconnection from discord before interrupting the thread
     */
    @Override
    public void interrupt() {
        gateway.logout();
        DiscordVerification.getInstance().getProxy().getLogger().info("Disconnecting Discord");
        super.interrupt();
    }
}
