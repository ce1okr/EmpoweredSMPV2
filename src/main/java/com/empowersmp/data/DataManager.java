package com.empowersmp.data;

import com.empowersmp.classes.PlayerClass;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {

    private final JavaPlugin plugin;
    private final File folder;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "playerdata");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::load);
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            save(data);
        }
    }

    public void saveAll() {
        for (PlayerData data : cache.values()) {
            save(data);
        }
    }

    private PlayerData load(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        File file = new File(folder, uuid.toString() + ".yml");
        if (!file.exists()) {
            return data;
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        String className = yml.getString("class", null);
        if (className != null) {
            PlayerClass playerClass = PlayerClass.fromString(className);
            if (playerClass != null) {
                data.setPlayerClass(playerClass);
                data.setLevel(yml.getInt("level", 0));
                data.setReceivedStartingKit(yml.getBoolean("receivedStartingKit", false));
            }
        }
        data.setPvpDeaths(yml.getInt("pvpDeaths", 0));
        return data;
    }

    public void save(PlayerData data) {
        File file = new File(folder, data.getUuid().toString() + ".yml");
        YamlConfiguration yml = new YamlConfiguration();
        if (data.getPlayerClass() != null) {
            yml.set("class", data.getPlayerClass().name());
            yml.set("level", data.getLevel());
            yml.set("receivedStartingKit", data.hasReceivedStartingKit());
        }
        yml.set("pvpDeaths", data.getPvpDeaths());
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save data for " + data.getUuid(), e);
        }
    }
}
