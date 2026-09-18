package com.empowersmp.data;

import com.empowersmp.classes.PlayerClass;

import java.util.UUID;

/**
 * One player's progress: which class they picked (if any) and their level
 * within that class (0 = just picked, gets the "Level 0" starting kit).
 */
public class PlayerData {

    private final UUID uuid;
    private PlayerClass playerClass; // null = hasn't picked yet
    private int level = 0;
    private boolean receivedStartingKit = false;

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
}
