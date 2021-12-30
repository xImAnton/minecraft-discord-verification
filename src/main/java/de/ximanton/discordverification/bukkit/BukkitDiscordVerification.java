package de.ximanton.discordverification.bukkit;

import de.ximanton.discordverification.DiscordVerification;
import de.ximanton.discordverification.IDiscordVerification;
import de.ximanton.discordverification.bukkit.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitDiscordVerification extends JavaPlugin implements IDiscordVerification {

    @Override
    public void onDisable() {
        DiscordVerification.getInstance().disable();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DiscordVerification.getInstance().setupBukkit(this, getConfig());

        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        getCommand("isverified").setExecutor(new CheckPlayerVerifiedCommand());
        getCommand("clearverifications").setExecutor(new ClearCommand());
        getCommand("verifiedplayers").setExecutor(new ListVerifiedPlayersCommand());
        getCommand("unverify").setExecutor(new UnverifyCommand());
        getCommand("verify").setExecutor(new VerifyCommand());
    }

    @Override
    public void kickPlayer(String ign, String reason) {
        Player p = Bukkit.getServer().getPlayer(ign);

        if (p == null) return;

        Bukkit.getScheduler().runTask(this, () -> p.kickPlayer(reason));
    }

}
