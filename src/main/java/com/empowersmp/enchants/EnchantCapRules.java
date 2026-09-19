package com.empowersmp.enchants;

import com.empowersmp.EmpowerSMP;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Resolves the max level a given enchantment may reach. PHASE 1: these are
 * flat caps for EVERYONE, with no class/level-based exceptions - there's no
 * real way to earn an elevated cap yet since Phase 2's abilities (Strength
 * L3, Vitality L3, Ranger L1) aren't built. Once those exist, capFor() will
 * start checking the player's class + level again to raise these for
 * qualifying players, the same way it will for the world locks.
 *
 *   - Sharpness/Smite/Bane of Arthropods: capped at 4 (vanilla max is 5)
 *   - Protection/Blast/Projectile/Fire Protection: capped at 3 (vanilla max is 4)
 *   - Power: capped at 4 (vanilla max is 5)
 */
public class EnchantCapRules {

    private static final Set<Enchantment> SHARPNESS_FAMILY = Set.of(
            Enchantment.SHARPNESS, Enchantment.SMITE, Enchantment.BANE_OF_ARTHROPODS);

    private static final Set<Enchantment> PROTECTION_FAMILY = Set.of(
            Enchantment.PROTECTION, Enchantment.BLAST_PROTECTION,
            Enchantment.PROJECTILE_PROTECTION, Enchantment.FIRE_PROTECTION);

    private final EmpowerSMP plugin;

    public EnchantCapRules(EmpowerSMP plugin) {
        this.plugin = plugin;
    }

    /** Max level `enchantment` may reach for `player`, or -1 for "not governed here". */
    public int capFor(Player player, Enchantment enchantment) {
        if (SHARPNESS_FAMILY.contains(enchantment)) return 4;
        if (PROTECTION_FAMILY.contains(enchantment)) return 3;
        if (enchantment.equals(Enchantment.POWER)) return 4;
        return -1;
    }

    /** Clamps `level` down to whatever `player` is allowed for `enchantment`. Never raises it. */
    public int clamp(Player player, Enchantment enchantment, int level) {
        int cap = capFor(player, enchantment);
        if (cap < 0) return level;
        return Math.min(level, cap);
    }

    public boolean isGoverned(Enchantment enchantment) {
        return SHARPNESS_FAMILY.contains(enchantment)
                || PROTECTION_FAMILY.contains(enchantment)
                || enchantment.equals(Enchantment.POWER);
    }
}
