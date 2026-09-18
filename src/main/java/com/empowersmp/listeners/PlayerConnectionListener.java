package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Frees cached data and persists it when a player leaves. */
public class PlayerConnectionListener implements Listener {

    private final EmpowerSMP plugin;

    public PlayerConnectionListener(EmpowerSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getDataManager().unload(event.getPlayer().getUniqueId());
    }
}
