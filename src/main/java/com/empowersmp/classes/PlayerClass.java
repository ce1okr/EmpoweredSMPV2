package com.empowersmp.classes;

/**
 * The 8 player classes. A player picks exactly one, once, and levels up
 * within it only (levels are NOT shared across classes). Max level differs
 * per class based on how many named ability tiers it has.
 */
public enum PlayerClass {
    STRENGTH("Strength", 7),
    DEFENSE("Defense", 4),
    MOBILITY("Mobility", 7),
    RANGER("Ranger", 5),
    VITALITY("Vitality", 5),
    ELEMENTAL("Elemental", 5),
    INVISIBILITY("Invisibility", 5),
    PROSPERITY("Prosperity", 5);

    private final String displayName;
    private final int maxLevel;

    PlayerClass(String displayName, int maxLevel) {
        this.displayName = displayName;
        this.maxLevel = maxLevel;
    }

    public String displayName() {
        return displayName;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public static PlayerClass fromString(String s) {
        for (PlayerClass c : values()) {
            if (c.name().equalsIgnoreCase(s) || c.displayName.equalsIgnoreCase(s)) {
                return c;
            }
        }
        return null;
    }

    public static int clamp(PlayerClass playerClass, int level) {
        return Math.max(0, Math.min(playerClass.maxLevel(), level));
    }
}
