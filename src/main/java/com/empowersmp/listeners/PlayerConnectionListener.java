package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.StartingKits;
import com.empowersmp.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Frees cached data and persists it when a player leaves. Also catches the
 * case where an admin used /empower setclass on a player while they were
 * offline - their starting kit couldn't be handed to them then, so it's
 * granted the moment they next join instead.
 */
public class PlayerConnectionListener implements Listener {

    private final EmpowerSMP plugin;

    public PlayerConnectionListener(EmpowerSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerData data = plugin.getDataManager().get(event.getPlayer().getUniqueId());
        if (data.getPlayerClass() != null && !data.hasReceivedStartingKit()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                StartingKits.grant(event.getPlayer(), data.getPlayerClass());
                data.setReceivedStartingKit(true);
                plugin.getDataManager().save(data);
                event.getPlayer().sendMessage(Component.text(
                        "You have been made " + data.getPlayerClass().displayName()
                                + "! Your starting kit has been given.", NamedTextColor.LIGHT_PURPLE));
            }, 5L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getDataManager().unload(event.getPlayer().getUniqueId());
    }
}
