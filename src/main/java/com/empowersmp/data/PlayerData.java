package com.empowersmp.data;

import com.empowersmp.classes.PlayerClass;

import java.util.UUID;

/**
 * One player's progress: which class they picked (if any), their level
 * within that class (0 = just picked, gets the "Level 0" starting kit), and
 * their PVP-death counter (0, 1, or 2 - reaching 2 drops them a level and
 * resets to 0).
 */
public class PlayerData {

    private final UUID uuid;
    private PlayerClass playerClass; // null = hasn't picked yet
    private int level = 0;
    private boolean receivedStartingKit = false;
    private int pvpDeaths = 0; // 0, 1, or 2 - hitting 2 triggers a level-down and resets to 0

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
        this.level = 0;
        this.receivedStartingKit = false;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (playerClass == null) return;
        this.level = PlayerClass.clamp(playerClass, level);
    }

    public void addLevel(int amount) {
        setLevel(level + amount);
    }

    public boolean hasReceivedStartingKit() {
        return receivedStartingKit;
    }

    public void setReceivedStartingKit(boolean receivedStartingKit) {
        this.receivedStartingKit = receivedStartingKit;
    }

    public int getPvpDeaths() {
        return pvpDeaths;
    }

    public void setPvpDeaths(int pvpDeaths) {
        this.pvpDeaths = Math.max(0, Math.min(2, pvpDeaths));
    }
}
