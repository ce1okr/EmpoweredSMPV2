package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.PlayerClass;
import com.empowersmp.data.DataManager;
import com.empowersmp.data.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Ranger's shot-streak procs. Both L3's "every 3rd shot does 1 true-damage
 * heart" and L5's "every 5th successful shot does a 5-heart Shotgun" count
 * consecutive HITS only - a miss doesn't advance or reset the streak. Per
 * the design clarification, L5 fully replaces L3 once unlocked (a Ranger at
 * L5 never fires the smaller L3 proc anymore), so only one branch below can
 * ever fire for a given player.
 *
 * "No matter the armor" is implemented by reducing health directly rather
 * than calling Entity#damage(), which bypasses the normal armor/enchantment
 * damage-reduction pipeline entirely.
 */
public class RangerShotListener implements Listener {

    private final DataManager dataManager;

    public RangerShotListener(EmpowerSMP plugin) {
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile projectile)) return;
        if (!(projectile.getShooter() instanceof Player shooter)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (victim.equals(shooter)) return;

        PlayerData data = dataManager.get(shooter.getUniqueId());
        if (data.getPlayerClass() != PlayerClass.RANGER) return;
        int level = data.getLevel();
        if (level < 3) return;

        int streak = data.incrementRangerShotStreak();
        dataManager.save(data);

        if (level >= 5) {
            if (streak % 5 == 0) {
                trueDamage(victim, 10.0); // 5 hearts
            }
        } else {
            if (streak % 3 == 0) {
                trueDamage(victim, 2.0); // 1 heart
            }
        }
    }

    private void trueDamage(LivingEntity victim, double amount) {
        double newHealth = Math.max(0.0, victim.getHealth() - amount);
        victim.setHealth(newHealth);
    }
}
