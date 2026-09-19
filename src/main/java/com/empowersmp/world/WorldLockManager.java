package com.empowersmp.world;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Tracks the three permanent, server-wide unlock flags (Nether, End,
 * Villagers). All three start locked for everyone and stay locked until an
 * OP runs /empower unlock <nether|end|villagers> - a one-way switch, there's
 * no re-lock command.
 */
public class WorldLockManager {

    private final JavaPlugin plugin;
    private final File file;
    private boolean netherUnlocked = false;
    private boolean endUnlocked = false;
    private boolean villagersUnlocked = false;

    public WorldLockManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "worldlocks.yml");
        load();
    }

    public boolean isNetherUnlocked() {
        return netherUnlocked;
    }

    public boolean isEndUnlocked() {
        return endUnlocked;
    }

    public boolean isVillagersUnlocked() {
        return villagersUnlocked;
    }

    public void unlockNether() {
        netherUnlocked = true;
        save();
    }

    public void unlockEnd() {
        endUnlocked = true;
        save();
    }

    public void unlockVillagers() {
        villagersUnlocked = true;
        save();
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        netherUnlocked = yml.getBoolean("netherUnlocked", false);
        endUnlocked = yml.getBoolean("endUnlocked", false);
        villagersUnlocked = yml.getBoolean("villagersUnlocked", false);
    }

    private void save() {
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("netherUnlocked", netherUnlocked);
        yml.set("endUnlocked", endUnlocked);
        yml.set("villagersUnlocked", villagersUnlocked);
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save worldlocks.yml", e);
        }
    }
} 
