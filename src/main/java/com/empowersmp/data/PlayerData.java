package com.empowersmp.data;

import com.empowersmp.classes.PlayerClass;

import java.util.UUID;

public class PlayerData {

    public enum BootChoice { NONE, DEPTH_STRIDER, FROST_WALKER }

    private final UUID uuid;
    private PlayerClass playerClass; // null = hasn't picked yet
    private int level = 0;
    private boolean receivedStartingKit = false;
    private int pvpDeaths = 0; // 0, 1, or 2 - hitting 2 triggers a level-down and resets to 0

    private BootChoice bootChoice = BootChoice.NONE;
    private boolean receivedCrossbow = false;
    private boolean receivedShield = false;
    private int rangerShotStreak = 0;

    private long berserkCooldownEnd = 0;
    private long lastStandCooldownEnd = 0;
    private long heartBurstCooldownEnd = 0;
    private long speedBlitzCooldownEnd = 0;

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
        this.bootChoice = BootChoice.NONE;
        this.receivedCrossbow = false;
        this.receivedShield = false;
        this.rangerShotStreak = 0;
        resetAbilityCooldowns();
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

    public BootChoice getBootChoice() {
        return bootChoice;
    }

    public void setBootChoice(BootChoice bootChoice) {
        this.bootChoice = bootChoice;
    }

    public boolean hasReceivedCrossbow() {
        return receivedCrossbow;
    }

    public void setReceivedCrossbow(boolean receivedCrossbow) {
        this.receivedCrossbow = receivedCrossbow;
    }

    public boolean hasReceivedShield() {
        return receivedShield;
    }

    public void setReceivedShield(boolean receivedShield) {
        this.receivedShield = receivedShield;
    }

    public int getRangerShotStreak() {
        return rangerShotStreak;
    }

    public void setRangerShotStreak(int rangerShotStreak) {
        this.rangerShotStreak = rangerShotStreak;
    }

    public int incrementRangerShotStreak() {
        return ++rangerShotStreak;
    }

    public long getBerserkCooldownEnd() {
        return berserkCooldownEnd;
    }

    public void setBerserkCooldownEnd(long berserkCooldownEnd) {
        this.berserkCooldownEnd = berserkCooldownEnd;
    }

    public long getLastStandCooldownEnd() {
        return lastStandCooldownEnd;
    }

    public void setLastStandCooldownEnd(long lastStandCooldownEnd) {
        this.lastStandCooldownEnd = lastStandCooldownEnd;
    }

    public long getHeartBurstCooldownEnd() {
        return heartBurstCooldownEnd;
    }

    public void setHeartBurstCooldownEnd(long heartBurstCooldownEnd) {
        this.heartBurstCooldownEnd = heartBurstCooldownEnd;
    }

    public long getSpeedBlitzCooldownEnd() {
        return speedBlitzCooldownEnd;
    }

    public void setSpeedBlitzCooldownEnd(long speedBlitzCooldownEnd) {
        this.speedBlitzCooldownEnd = speedBlitzCooldownEnd;
    }

    /** Dying wipes all auto-trigger ability cooldowns back to 0 (ready), per design. */
    public void resetAbilityCooldowns() {
        berserkCooldownEnd = 0;
        lastStandCooldownEnd = 0;
        heartBurstCooldownEnd = 0;
        speedBlitzCooldownEnd = 0;
    }
}
