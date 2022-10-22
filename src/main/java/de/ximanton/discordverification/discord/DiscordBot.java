package de.ximanton.discordverification.discord;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.discord.command.IGNCommand;
import de.ximanton.discordverification.discord.command.VerifyCommand;
import de.ximanton.discordverification.discord.command.WhoIsCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The class to manage the discord bot user.
 * This is a Thread to not block the entire Proxy while listening for discord events
 */
public class DiscordBot extends Thread implements EventListener {

    private static final Pattern commandPattern = Pattern.compile("!([^ ]+)(.*)");

    private JDA client;
    private final String token;
    private Guild guild;
    private final LinkedList<Role> rolesToAdd = new LinkedList<>();
    private final LinkedList<Role> rolesToRemove = new LinkedList<>();

    private final Map<String, Command> commands = new HashMap<>();

    public void registerCommand(Command cmd) {
        commands.put(cmd.getName(), cmd);
    }

    /**
     * @param token The discord token to connect to the gateway
     */
    public DiscordBot(String token) {
        this.token = token;
        registerCommand(new VerifyCommand());
        registerCommand(new IGNCommand());
        registerCommand(new WhoIsCommand());
    }

    /**
     * Called when a new discord message is received
     * calls the commands when a command is detected
     *
     * @param e the MessageCreateEvent
     */
    public void onMessage(MessageReceivedEvent e) {
        final Message msg = e.getMessage();
        // if the message is on the wrong guild, do nothing
        if (msg.getGuild().getIdLong() != DiscordVerification.getInstance().getGuildId()) return;

        Matcher commandMatcher = commandPattern.matcher(msg.getContentRaw());

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
     *
     * @param e the MemberLeaveEvent
     */
    public void onMemberLeave(GuildMemberRemoveEvent e) {
        if (e.getGuild().getIdLong() != DiscordVerification.getInstance().getGuildId()) return;
        DiscordVerification.getInstance().getDB().removeAccountOfUser(e.getUser().getIdLong());
        updateStatus();
    }

    /**
     * Updates roles of a newly verified/unverified member according to the config
     *
     * @param ign the ign that was verified/unverified
     * @param wasVerified whether the user was verified
     */
    public void performRoleUpdate(String ign, boolean wasVerified) throws SQLException {
        long userId = DiscordVerification.getInstance().getDB().getUserIdForIGN(ign);
        if (userId == 0) return;

        performRoleUpdate(userId, wasVerified);
    }

    /**
     * Updates roles of a newly verified/unverified member according to the config
     *
     * @param userId the discord user id of the user that was verified/unverified
     * @param wasVerified whether the user was verified
     */
    public void performRoleUpdate(long userId, boolean wasVerified) {
        if (userId == 0) return;

        RestActionStack requests = new RestActionStack();

        for (Role roleToAdd : rolesToAdd) {
            if (wasVerified) {
                requests.add(guild.addRoleToMember(userId, roleToAdd));
            } else {
                requests.add(guild.removeRoleFromMember(userId, roleToAdd));
            }
        }

        for (Role roleToRemove : rolesToRemove) {
            if (wasVerified) {
                requests.add(guild.removeRoleFromMember(userId, roleToRemove));
            } else {
                requests.add(guild.addRoleToMember(userId, roleToRemove));
            }
        }

        requests.queue();
    }

    /**
     * The overridden Thread#run
     */
    @Override
    public void run() {
        try {
            client = JDABuilder.createDefault(token)
                    .addEventListeners(this)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES)
                    .build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(@NotNull GenericEvent e) {
        if (e instanceof ReadyEvent) {
            onReady();
        } else if (e instanceof MessageReceivedEvent) {
            onMessage((MessageReceivedEvent) e);
        } else if (e instanceof GuildMemberRemoveEvent) {
            onMemberLeave((GuildMemberRemoveEvent) e);
        }
    }

    private void onReady() {
        DiscordVerification.getInstance().getPlugin().getLogger().info("Discord is ready!");

        guild = client.getGuildById(DiscordVerification.getInstance().getGuildId());

        for (long roleToAdd : DiscordVerification.getInstance().getRolesToAdd()) {
            rolesToAdd.add(guild.getRoleById(roleToAdd));
        }
        for (long roleToRemove : DiscordVerification.getInstance().getRolesToRemove()) {
            rolesToRemove.add(guild.getRoleById(roleToRemove));
        }

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

        client.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing(DiscordVerification.getInstance().getMessages().formatStatus(limit, count)));
    }

    /**
     * Disconnection from discord before interrupting the thread
     */
    @Override
    public void interrupt() {
        client.shutdown();
        DiscordVerification.getInstance().getPlugin().getLogger().info("Disconnecting Discord");
        super.interrupt();
    }

    public JDA getClient() {
        return client;
    }

}
