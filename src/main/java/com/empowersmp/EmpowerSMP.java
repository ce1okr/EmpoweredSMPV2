package com.empowersmp;

import com.empowersmp.commands.EmpowerCommand;
import com.empowersmp.data.DataManager;
import com.empowersmp.listeners.EnchantCapListener;
import com.empowersmp.listeners.PlayerConnectionListener;
import com.empowersmp.listeners.WorldLockListener;
import com.empowersmp.world.WorldLockManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EmpowerSMP extends JavaPlugin {

    private DataManager dataManager;
    private WorldLockManager worldLockManager;
    private EnchantCapListener enchantCapListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.dataManager = new DataManager(this);
        this.worldLockManager = new WorldLockManager(this);
        this.enchantCapListener = new EnchantCapListener(this);

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldLockListener(this), this);
        getServer().getPluginManager().registerEvents(enchantCapListener, this);

        EmpowerCommand cmd = new EmpowerCommand(this);
        getCommand("empower").setExecutor(cmd);
        getCommand("empower").setTabCompleter(cmd);

        long autosaveTicks = 5L * 60L * 20L;
        Bukkit.getScheduler().runTaskTimer(this, () -> dataManager.saveAll(), autosaveTicks, autosaveTicks);

        // Fallback re-scan every 15s to catch enchant-cap violations from sources
        // other than anvils/enchanting tables/clicks (commands, other plugins, etc).
        Bukkit.getScheduler().runTaskTimer(this,
                () -> Bukkit.getOnlinePlayers().forEach(enchantCapListener::periodicSweep),
                300L, 300L);

        getLogger().info("EmpowerSMP v2 (Phase 1) enabled.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        getLogger().info("EmpowerSMP disabled.");
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public WorldLockManager getWorldLockManager() {
        return worldLockManager;
    }
}
