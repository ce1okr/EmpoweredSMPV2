package com.empowersmp.enchants;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.PlayerClass;
import com.empowersmp.data.PlayerData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Resolves the max level a given enchantment may reach for a given player,
 * based on server-wide base caps and their class + level:
 *   - Sharpness/Smite/Bane of Arthropods: base 4, raised to 5 at Strength L3
 *   - Protection/Blast/Projectile/Fire Protection: base 3, raised to 4 at Vitality L3
 *   - Power: base 4, raised to 5 at Ranger L1
 * Quick Charge is NOT handled here - Ranger L4's Quick Charge X crossbow is a
 * directly-granted custom item, not an anvil-combinable cap.
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
        PlayerClass playerClass = data.getPlayerClass();
        int level = data.getLevel();

        if (SHARPNESS_FAMILY.contains(enchantment)) {
            boolean elevated = playerClass == PlayerClass.STRENGTH && level >= 3;
            return elevated ? 5 : 4;
        }
        if (PROTECTION_FAMILY.contains(enchantment)) {
            boolean elevated = playerClass == PlayerClass.VITALITY && level >= 3;
            return elevated ? 4 : 3;
        }
        if (enchantment.equals(Enchantment.POWER)) {
            boolean elevated = playerClass == PlayerClass.RANGER && level >= 1;
            return elevated ? 5 : 4;
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
                || enchantment.equals(Enchantment.POWER);
    }
}
