package com.empowersmp.enchants;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.PlayerClass;
import com.empowersmp.data.PlayerData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Resolves the max level a given enchantment may reach. PHASE 2: caps are
 * now class+level aware, matching the ability lists:
 *   - Sharpness family: 4 for everyone, 5 for Strength L3+
 *   - Protection family: 3 for everyone, 4 for Vitality L3+
 *   - Power: 4 for everyone, 5 for Ranger L1+
 *   - Quick Charge: 3 (vanilla) for everyone, 10 for Ranger L4+ (this is the
 *     custom override used on the one-time granted crossbow only)
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
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        PlayerClass pc = data.getPlayerClass();
        int level = data.getLevel();

        if (SHARPNESS_FAMILY.contains(enchantment)) {
            return (pc == PlayerClass.STRENGTH && level >= 3) ? 5 : 4;
        }
        if (PROTECTION_FAMILY.contains(enchantment)) {
            return (pc == PlayerClass.VITALITY && level >= 3) ? 4 : 3;
        }
        if (enchantment.equals(Enchantment.POWER)) {
            return (pc == PlayerClass.RANGER && level >= 1) ? 5 : 4;
        }
        if (enchantment.equals(Enchantment.QUICK_CHARGE)) {
            return (pc == PlayerClass.RANGER && level >= 4) ? 10 : 3;
        }
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
                || enchantment.equals(Enchantment.POWER)
                || enchantment.equals(Enchantment.QUICK_CHARGE);
    }
}
