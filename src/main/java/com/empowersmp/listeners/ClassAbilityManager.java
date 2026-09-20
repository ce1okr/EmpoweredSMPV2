package com.empowersmp.classes;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.data.DataManager;
import com.empowersmp.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies each class's permanent passive perks (potion effects, max health,
 * one-time gear grants) based on current class + level. "Upgrade replaces
 * base" is implemented by fully clearing every effect type this system
 * manages and recomputing from scratch every time - call refresh() on join,
 * on any level change, and after respawn (dying clears infinite-duration
 * effects, so they need to be reapplied).
 *
 * Things NOT handled here (they live in their own listeners):
 *   - Enchant caps (Sharpness 5, Protection 4, Power 5, Quick Charge 10)  -> EnchantCapRules
 *   - Floor-enforced enchants (Fortune 3, Looting 3, Fire Aspect 2,
 *     Power 5 on bows, Mobility's helmet/boot enchants)                  -> MinimumEnchantListener
 *   - Netherite exclusivity                                              -> NetheriteBanListener
 *   - Potion/effect exclusivity                                          -> PotionRestrictionListener / ExclusiveEffectListener
 *   - Auto-trigger abilities (Berserk, Last Stand, Heart Burst,
 *     Speed Blitz)                                                       -> AutoAbilityListener
 *   - Ranger's shot-streak procs                                         -> RangerShotListener
 *   - Elemental's Ice Breaker                                            -> IceBreakerListener
 *
 * UNRESOLVED - flagged rather than guessed:
 *   - Mobility L3 "Weaving": there is no vanilla Bukkit PotionEffectType
 *     called Weaving as of 1.21. Not implemented until you confirm what
 *     effect this actually refers to.
 *   - Prosperity L4 "2x Potion Effects": ambiguous (double duration? double
 *     amplifier? something else?). Not implemented until clarified.
 *   - Prosperity L3 "2x XP Boost": needs a PlayerExpChangeEvent multiplier;
 *     not wired up yet - flag if you want this built out too.
 */
public class ClassAbilityManager {

    private static final int INFINITE = PotionEffect.INFINITE_DURATION;

    private static final PotionEffectType[] MANAGED = {
            PotionEffectType.SPEED, PotionEffectType.STRENGTH, PotionEffectType.REGENERATION,
            PotionEffectType.FIRE_RESISTANCE, PotionEffectType.WATER_BREATHING, PotionEffectType.DOLPHINS_GRACE,
            PotionEffectType.NIGHT_VISION, PotionEffectType.INVISIBILITY, PotionEffectType.ABSORPTION,
            PotionEffectType.RESISTANCE, PotionEffectType.HERO_OF_THE_VILLAGE
    };

    private final DataManager dataManager;

    public ClassAbilityManager(EmpowerSMP plugin) {
        this.dataManager = plugin.getDataManager();
    }

    /** Recomputes everything this system manages from scratch. Call on join, level change, and respawn. */
    public void refresh(Player player) {
        PlayerData data = dataManager.get(player.getUniqueId());
        PlayerClass pc = data.getPlayerClass();
        int level = data.getLevel();

        for (PotionEffectType type : MANAGED) {
            player.removePotionEffect(type);
        }
        player.setInvisible(false);

        if (pc == null) {
            setMaxHealth(player, 20.0);
            return;
        }

        switch (pc) {
            case STRENGTH -> applyStrength(player, level);
            case DEFENSE -> applyDefense(player, level);
            case MOBILITY -> applyMobility(player, level);
            case RANGER -> applyRanger(player, level);
            case VITALITY -> applyVitality(player, level);
            case ELEMENTAL -> applyElemental(player, level);
            case INVISIBILITY -> applyInvisibility(player, level);
            case PROSPERITY -> applyProsperity(player, level);
        }

        handleOneTimeGrants(player, data, pc, level);
    }

    private void permanent(Player player, PotionEffectType type, int amplifier) {
        player.addPotionEffect(new PotionEffect(type, INFINITE, amplifier, true, false, false));
    }

    // ---------------- STRENGTH ----------------
    // L1 Fire Aspect II (floor, elsewhere)
    // L2 Strength I permanent
    // L3 Sharpness 5 usable (cap, elsewhere)
    // L4 Strength II permanent (replaces I)
    // L5 Berserk (auto-trigger, elsewhere)
    private void applyStrength(Player player, int level) {
        if (level >= 4) permanent(player, PotionEffectType.STRENGTH, 1);
        else if (level >= 2) permanent(player, PotionEffectType.STRENGTH, 0);
        setMaxHealth(player, 20.0);
    }

    // ---------------- DEFENSE ----------------
    // L1 Unbreakable Shield (one-time grant, below)
    // L2 Absorption I permanent
    // L3 Netherite armor usable (elsewhere)
    // L4 Resistance I permanent
    // L5 Last Stand (auto-trigger, elsewhere)
    private void applyDefense(Player player, int level) {
        if (level >= 2) permanent(player, PotionEffectType.ABSORPTION, 0);
        if (level >= 4) permanent(player, PotionEffectType.RESISTANCE, 0);
        setMaxHealth(player, 20.0);
    }

    // ---------------- MOBILITY ----------------
    // L1 boot/helmet enchants + one-time boot choice (elsewhere)
    // L2 Speed I permanent
    // L3 "Weaving" - UNRESOLVED, see class note
    // L4 Speed II permanent (replaces I)
    // L5 Speed Blitz (auto-trigger, elsewhere)
    private void applyMobility(Player player, int level) {
        if (level >= 4) permanent(player, PotionEffectType.SPEED, 1);
        else if (level >= 2) permanent(player, PotionEffectType.SPEED, 0);
        setMaxHealth(player, 20.0);
    }

    // ---------------- RANGER ----------------
    // L1 Power 5 (floor, elsewhere)
    // L2 Speed I permanent
    // L3/L5 shot procs (elsewhere)
    // L4 crossbow one-time grant (below)
    private void applyRanger(Player player, int level) {
        if (level >= 2) permanent(player, PotionEffectType.SPEED, 0);
        setMaxHealth(player, 20.0);
    }

    // ---------------- VITALITY ----------------
    // Hearts: L0=11(22hp, StartingKits) L1=12(24) L2=14(28) L3=16(32) L4=17(34) L5=17(34, Heart Burst only)
    // L3 Protection 4 usable (cap, elsewhere)
    // L4 Regeneration II permanent
    // L5 Heart Burst (auto-trigger, elsewhere)
    private void applyVitality(Player player, int level) {
        double hp = switch (level) {
            case 1 -> 24.0;
            case 2 -> 28.0;
            case 3 -> 32.0;
            case 4, 5 -> 34.0;
            default -> 22.0;
        };
        setMaxHealth(player, hp);
        if (level >= 4) permanent(player, PotionEffectType.REGENERATION, 1);
    }

    // ---------------- ELEMENTAL ----------------
    // L1 Fire Resistance permanent
    // L2 Fire Aspect II (floor, elsewhere)
    // L3 Water Breathing permanent
    // L4 Dolphin's Grace I permanent
    // L5 Ice Breaker (elsewhere)
    private void applyElemental(Player player, int level) {
        if (level >= 1) permanent(player, PotionEffectType.FIRE_RESISTANCE, 0);
        if (level >= 3) permanent(player, PotionEffectType.WATER_BREATHING, 0);
        if (level >= 4) permanent(player, PotionEffectType.DOLPHINS_GRACE, 0);
        setMaxHealth(player, 20.0);
    }

    // ---------------- INVISIBILITY ----------------
    // L1 Night Vision permanent
    // L2 invisible only while crouching - overshadowed permanently once L3 is reached
    // L3 Invisibility permanent (with particles, replaces L2)
    // L4 no footstep particles (best-effort via metadata invisible flag)
    // L5 no invisibility particles (particles=false on the effect itself)
    private void applyInvisibility(Player player, int level) {
        if (level >= 1) permanent(player, PotionEffectType.NIGHT_VISION, 0);
        if (level >= 3) {
            boolean hideParticles = level >= 5;
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY, INFINITE, 0, true, !hideParticles, false));
        }
        // Metadata-level invisible flag (distinct from the potion effect) - this is what
        // actually suppresses the client-side sprint/footstep dust particles that would
        // otherwise give away an invisible player's position. Only applied at L4+.
        player.setInvisible(level >= 4);
        setMaxHealth(player, 20.0);
    }

    // ---------------- PROSPERITY ----------------
    // L1 Fortune 3 (floor, elsewhere)
    // L2 Looting 3 (floor, elsewhere) + Hero of the Village V
    // L3 2x XP (UNRESOLVED, see class note) + Netherite tools usable (elsewhere)
    // L4 2x Potion Effects (UNRESOLVED, see class note)
    // L5 Hero of the Village X (replaces V)
    private void applyProsperity(Player player, int level) {
        if (level >= 5) permanent(player, PotionEffectType.HERO_OF_THE_VILLAGE, 9);
        else if (level >= 2) permanent(player, PotionEffectType.HERO_OF_THE_VILLAGE, 4);
        setMaxHealth(player, 20.0);
    }

    private void setMaxHealth(Player player, double hp) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;
        double old = attr.getBaseValue();
        if (old == hp) return;
        attr.setBaseValue(hp);
        if (hp > old) {
            player.setHealth(Math.min(hp, player.getHealth() + (hp - old)));
        } else {
            player.setHealth(Math.min(player.getHealth(), hp));
        }
    }

    private void handleOneTimeGrants(Player player, PlayerData data, PlayerClass pc, int level) {
        if (pc == PlayerClass.DEFENSE && level >= 1 && !data.hasReceivedShield()) {
            ItemStack shield = new ItemStack(Material.SHIELD);
            ItemMeta meta = shield.getItemMeta();
            meta.setUnbreakable(true);
            shield.setItemMeta(meta);
            player.getInventory().addItem(shield);
            data.setReceivedShield(true);
            dataManager.save(data);
            player.sendMessage(Component.text("You've been granted an unbreakable shield!", NamedTextColor.LIGHT_PURPLE));
        }
        if (pc == PlayerClass.RANGER && level >= 4 && !data.hasReceivedCrossbow()) {
            player.getInventory().addItem(bestCrossbow());
            data.setReceivedCrossbow(true);
            dataManager.save(data);
            player.sendMessage(Component.text("You've been granted a fully-enchanted crossbow!", NamedTextColor.LIGHT_PURPLE));
        }
    }

    private ItemStack bestCrossbow() {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        // Unsafe/override enchants, matching the pattern used elsewhere for "beyond vanilla" gear.
        // Multishot + Piercing don't normally coexist in vanilla; addUnsafeEnchantment bypasses
        // that conflict deliberately, since this is meant to be a best-possible loadout.
        crossbow.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 10);
        crossbow.addUnsafeEnchantment(Enchantment.MULTISHOT, 1);
        crossbow.addUnsafeEnchantment(Enchantment.PIERCING, 4);
        crossbow.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        crossbow.addUnsafeEnchantment(Enchantment.MENDING, 1);
        return crossbow;
    }
}
