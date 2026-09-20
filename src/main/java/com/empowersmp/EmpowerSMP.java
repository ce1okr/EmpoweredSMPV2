package com.empowersmp;

import com.empowersmp.commands.EmpowerCommand;
import com.empowersmp.data.DataManager;
import com.empowersmp.items.CraftingRecipes;
import com.empowersmp.listeners.EnchantCapListener;
import com.empowersmp.listeners.LevelUpgraderListener;
import com.empowersmp.listeners.NetheriteBanListener;
import com.empowersmp.listeners.PlayerConnectionListener;
import com.empowersmp.listeners.PlayerDeathEconomyListener;
import com.empowersmp.listeners.PotionRestrictionListener;
import com.empowersmp.listeners.WorldLockListener;
import com.empowersmp.world.WorldLockManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EmpowerSMP extends JavaPlugin {

    private DataManager dataManager;
    private WorldLockManager worldLockManager;
    private EnchantCapListener enchantCapListener;
    private NetheriteBanListener netheriteBanListener;
    private PotionRestrictionListener potionRestrictionListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.dataManager = new DataManager(this);
        this.worldLockManager = new WorldLockManager(this);
        this.enchantCapListener = new EnchantCapListener(this);
        this.netheriteBanListener = new NetheriteBanListener(this);
        this.potionRestrictionListener = new PotionRestrictionListener(this);

        CraftingRecipes.registerAll(this);

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldLockListener(this), this);
        getServer().getPluginManager().registerEvents(enchantCapListener, this);
        getServer().getPluginManager().registerEvents(netheriteBanListener, this);
        getServer().getPluginManager().registerEvents(potionRestrictionListener, this);
        getServer().getPluginManager().registerEvents(new PlayerDeathEconomyListener(this), this);
        getServer().getPluginManager().registerEvents(new LevelUpgraderListener(this), this);

        EmpowerCommand cmd = new EmpowerCommand(this);
        getCommand("empower").setExecutor(cmd);
        getCommand("empower").setTabCompleter(cmd);

        long autosaveTicks = 5L * 60L * 20L;
        Bukkit.getScheduler().runTaskTimer(this, () -> dataManager.saveAll(), autosaveTicks, autosaveTicks);

        // Fallback re-scans every 15s to catch violations from sources other than
        // the events above (commands, other plugins, etc).
        Bukkit.getScheduler().runTaskTimer(this,
                () -> Bukkit.getOnlinePlayers().forEach(enchantCapListener::periodicSweep),
                300L, 300L);
        Bukkit.getScheduler().runTaskTimer(this,
                () -> Bukkit.getOnlinePlayers().forEach(netheriteBanListener::periodicSweep),
                300L, 300L);
        Bukkit.getScheduler().runTaskTimer(this,
                () -> Bukkit.getOnlinePlayers().forEach(potionRestrictionListener::periodicSweep),
                300L, 300L);

        getLogger().info("EmpowerSMP v2 enabled.");
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
