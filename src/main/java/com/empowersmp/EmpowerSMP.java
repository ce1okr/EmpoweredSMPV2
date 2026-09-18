package com.empowersmp;

import com.empowersmp.commands.EmpowerCommand;
import com.empowersmp.data.DataManager;
import com.empowersmp.listeners.PlayerConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EmpowerSMP extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.dataManager = new DataManager(this);

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

        EmpowerCommand cmd = new EmpowerCommand(this);
        getCommand("empower").setExecutor(cmd);
        getCommand("empower").setTabCompleter(cmd);

        long autosaveTicks = 5L * 60L * 20L;
        Bukkit.getScheduler().runTaskTimer(this, () -> dataManager.saveAll(), autosaveTicks, autosaveTicks);

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
}
