package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.PlayerClass;
import com.empowersmp.data.DataManager;
import com.empowersmp.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Elemental L5 "Ice Breaker": sprinting continuously on ice for 10 straight
 * seconds grants Speed III, refreshed every check as long as they keep
 * sprinting on ice, and left to lapse (vanilla effect expiry) shortly after
 * they stop. Runs as a once-per-second repeating task (registered in
 * EmpowerSMP) rather than an event, since there's no clean Bukkit event for
 * "still standing on the same block type N seconds later".
 */
public class IceBreakerListener {

    private static final Set<Material> ICE_BLOCKS = Set.of(
            Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE, Material.FROSTED_ICE);
    private static final int SECONDS_REQUIRED = 10;

    private final DataManager dataManager;
    private final Map<UUID, Integer> streakSeconds = new HashMap<>();

    public IceBreakerListener(EmpowerSMP plugin) {
        this.dataManager = plugin.getDataManager();
    }

    /** Call once per second for every online player. */
    public void tick(Player player) {
        PlayerData data = dataManager.get(player.getUniqueId());
        if (data.getPlayerClass() != PlayerClass.ELEMENTAL || data.getLevel() < 5) {
            streakSeconds.remove(player.getUniqueId());
            return;
        }

        boolean onIce = ICE_BLOCKS.contains(player.getLocation().subtract(0, 1, 0).getBlock().getType());
        boolean qualifies = onIce && player.isSprinting();

        if (qualifies) {
            int seconds = streakSeconds.merge(player.getUniqueId(), 1, Integer::sum);
            if (seconds >= SECONDS_REQUIRED) {
                // Duration is refreshed every second they keep qualifying; lapses ~2s after they stop.
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, false, true, true));
            }
        } else {
            streakSeconds.remove(player.getUniqueId());
        }
    }
}
