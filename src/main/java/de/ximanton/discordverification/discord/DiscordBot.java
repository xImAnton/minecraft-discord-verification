package de.ximanton.discordverification.discord;

import de.ximanton.discordverification.DiscordVerification;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The class to manage the discord bot user.
 * This is a Thread to not block the entire Proxy while listening for discord events
 */
public class DiscordBot extends Thread {

    private static final Pattern commandPattern = Pattern.compile("!([^ ]+)(.*)");

    private final DiscordClient client;
    private GatewayDiscordClient gateway;

    private final Map<String, Command> commands = new HashMap<>();

    public void registerCommand(Command cmd) {
        commands.put(cmd.getName(), cmd);
    }

    /**
     * @param token The discord token to connect to the gateway
     */
    public DiscordBot(String token) {
        this.client = DiscordClient.create(token);
        registerCommand(new VerifyCommand());
        registerCommand(new IGNCommand());
        registerCommand(new WhoIsCommand());
    }

    /**
     * Called when a new discord message is received
     * calls the commands when a command is detected
     * @param e the MessageCreateEvent
     */
    public void onMessage(MessageCreateEvent e) {
        final Message msg = e.getMessage();
        // if the message is on the wrong guild, do nothing
        if (!msg.getGuild().block().getId().asBigInteger().equals(DiscordVerification.getInstance().getGuildId())) return;

        Matcher commandMatcher = commandPattern.matcher(msg.getContent());

        if (!commandMatcher.matches()) { // not a command
            return;
        }

        String command = commandMatcher.group(1);

        if (!commands.containsKey(command)) {
            return;
        }

        String[] args = commandMatcher.group(2).split(" ");
        args = Arrays.copyOfRange(args, 1, args.length);

        commands.get(command).dispatch(msg, args);
    }

    /**
     * Removes the account of a user if he leaves the guild
     * @param e the MemberLeaveEvent
     */
    public void onMemberLeave(MemberLeaveEvent e) {
        if (!e.getGuild().block().getId().asBigInteger().equals(DiscordVerification.getInstance().getGuildId())) return;
        DiscordVerification.getInstance().getDB().removeAccountOfUser(e.getUser().getId().asLong());
        updateStatus();
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
        gateway.on(ReadyEvent.class).subscribe(this::onReady);
        gateway.on(MemberLeaveEvent.class).subscribe(this::onMemberLeave);
    }

    private void onReady(ReadyEvent readyEvent) {
        DiscordVerification.getInstance().getProxy().getLogger().info("Discord is ready!");
        updateStatus();
    }

    public void updateStatus() {
        int limit = DiscordVerification.getInstance().getVerificationLimit();
        int count = 0;

        try {
            count = DiscordVerification.getInstance().getDB().getVerificationCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        gateway.updatePresence(Presence.online(Activity.playing(DiscordVerification.getInstance().getMessages().formatStatus(limit, count)))).block();
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

    public DiscordClient getClient() {
        return client;
    }

    public GatewayDiscordClient getGateway() {
        return gateway;
    }
}
