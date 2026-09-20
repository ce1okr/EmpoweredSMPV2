package com.empowersmp.classes;

/**
 * The 8 player classes. A player is assigned exactly one (by an admin, via
 * /empower setclass) and levels up within it only. All 8 classes cap at
 * level 5.
 */
public enum PlayerClass {
    STRENGTH("Strength"),
    DEFENSE("Defense"),
    MOBILITY("Mobility"),
    RANGER("Ranger"),
    VITALITY("Vitality"),
    ELEMENTAL("Elemental"),
    INVISIBILITY("Invisibility"),
    PROSPERITY("Prosperity");

    public static final int MAX_LEVEL = 5;

    private final String displayName;

    PlayerClass(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public int maxLevel() {
        return MAX_LEVEL;
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
