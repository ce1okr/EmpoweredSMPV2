package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.PlayerClass;
import com.empowersmp.data.DataManager;
import com.empowersmp.data.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Blocks the EFFECT itself (not just the drinkable potion item) for the
 * class-exclusive tiers, no matter how it was applied (splash potion thrown
 * by someone else, lingering cloud, beacon, /effect give, etc):
 *   - Invisibility (any amplifier): Invisibility class only
 *   - Strength II+ (amplifier >= 1): Strength class only - Strength I is
 *     unrestricted, anyone can have it
 *   - Speed II+ (amplifier >= 1): Mobility class only - Speed I is
 *     unrestricted
 *   - Fire Resistance (any amplifier): Elemental class only
 *
 * ClassAbilityManager only ever grants these to the matching class, so this
 * listener never blocks a player's own legitimate passives - it only stops
 * someone from getting an exclusive effect from an outside source.
 */
public class ExclusiveEffectListener implements Listener {

    private final DataManager dataManager;

    public ExclusiveEffectListener(EmpowerSMP plugin) {
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onEffect(EntityPotionEffectEvent event) {
        if (event.getNewEffect() == null) return; // an effect being removed, not applied
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        PotionEffectType type = event.getNewEffect().getType();
        int amplifier = event.getNewEffect().getAmplifier();
        PlayerData data = dataManager.get(player.getUniqueId());
        PlayerClass pc = data.getPlayerClass();

        boolean disallowed = false;
        if (type.equals(PotionEffectType.INVISIBILITY) && pc != PlayerClass.INVISIBILITY) disallowed = true;
        if (type.equals(PotionEffectType.STRENGTH) && amplifier >= 1 && pc != PlayerClass.STRENGTH) disallowed = true;
        if (type.equals(PotionEffectType.SPEED) && amplifier >= 1 && pc != PlayerClass.MOBILITY) disallowed = true;
        if (type.equals(PotionEffectType.FIRE_RESISTANCE) && pc != PlayerClass.ELEMENTAL) disallowed = true;

        if (disallowed) {
            event.setCancelled(true);
        }
    }
}
