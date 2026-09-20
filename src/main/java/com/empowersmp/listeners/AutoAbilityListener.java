package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.PlayerClass;
import com.empowersmp.data.DataManager;
import com.empowersmp.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.LongConsumer;

/**
 * The 4 auto-trigger, HP-threshold, cooldown-gated abilities. Any HP loss
 * (PVP, mobs, fall, lava, starvation, etc) can trigger these - per design,
 * not PVP-only. Cooldowns live on PlayerData and persist across restarts;
 * dying resets all 4 back to 0 (ready), handled in PlayerDeathEconomyListener.
 * The small on-screen cooldown timer is CooldownDisplayTask, ticking once a
 * second off these same fields.
 */
public class AutoAbilityListener implements Listener {

    private final DataManager dataManager;

    public AutoAbilityListener(EmpowerSMP plugin) {
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.isDead()) return;

        double healthAfter = player.getHealth() - event.getFinalDamage();
        if (healthAfter <= 0) return; // this hit is killing them, not triggering an ability

        PlayerData data = dataManager.get(player.getUniqueId());
        PlayerClass pc = data.getPlayerClass();
        if (pc == null) return;
        int level = data.getLevel();
        long now = System.currentTimeMillis();

        if (pc == PlayerClass.STRENGTH && level >= 5 && healthAfter <= 8.0 && now >= data.getBerserkCooldownEnd()) {
            trigger(player, data, data::setBerserkCooldownEnd, 70, "Berserk",
                    new PotionEffect(PotionEffectType.STRENGTH, 200, 3, false, true, true));
        }
        if (pc == PlayerClass.DEFENSE && level >= 5 && healthAfter <= 6.0 && now >= data.getLastStandCooldownEnd()) {
            trigger(player, data, data::setLastStandCooldownEnd, 120, "Last Stand",
                    new PotionEffect(PotionEffectType.RESISTANCE, 200, 4, false, true, true));
        }
        if (pc == PlayerClass.MOBILITY && level >= 5 && healthAfter <= 8.0 && now >= data.getSpeedBlitzCooldownEnd()) {
            trigger(player, data, data::setSpeedBlitzCooldownEnd, 70, "Speed Blitz",
                    new PotionEffect(PotionEffectType.SPEED, 200, 4, false, true, true));
        }
        if (pc == PlayerClass.VITALITY && level >= 5 && healthAfter <= 10.0 && now >= data.getHeartBurstCooldownEnd()) {
            triggerHeal(player, data);
        }
    }

    private void trigger(Player player, PlayerData data, LongConsumer setCooldown, int cooldownSeconds,
                          String name, PotionEffect effect) {
        setCooldown.accept(System.currentTimeMillis() + cooldownSeconds * 1000L);
        dataManager.save(data);
        player.addPotionEffect(effect);
        player.sendMessage(Component.text(name + " activated! (" + cooldownSeconds + "s cooldown)", NamedTextColor.GOLD));
    }

    private void triggerHeal(Player player, PlayerData data) {
        data.setHeartBurstCooldownEnd(System.currentTimeMillis() + 180 * 1000L);
        dataManager.save(data);
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) player.setHealth(attr.getValue());
        player.sendMessage(Component.text("Heart Burst activated! (180s cooldown)", NamedTextColor.GOLD));
    }
}
