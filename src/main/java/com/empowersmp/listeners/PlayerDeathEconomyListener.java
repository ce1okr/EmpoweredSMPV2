package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.data.DataManager;
import com.empowersmp.data.PlayerData;
import com.empowersmp.items.CustomItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * The Rank Up / level-loss economy. On a PVP kill only (natural deaths do
 * nothing here):
 *   - If the victim's level is 1-5, a fresh Rank Up drops for the killer.
 *   - The victim's own held Rank Ups decrease by 1, if they have any.
 *   - The victim's PVP-death counter ticks up (shown as "X/2"); at 2, their
 *     class level drops by 1 and the counter resets to 0.
 *
 * PHASE 2: any death (not just PVP - this part runs regardless of killer)
 * resets the victim's 4 auto-trigger ability cooldowns back to 0 (ready)
 * and their Ranger shot streak back to 0, per design. Infinite/permanent
 * potion effects tied to the player's class+level are restored automatically
 * on respawn by PlayerConnectionListener calling ClassAbilityManager#refresh.
 */
public class PlayerDeathEconomyListener implements Listener {

    private final DataManager dataManager;

    public PlayerDeathEconomyListener(EmpowerSMP plugin) {
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        PlayerData victimData = dataManager.get(victim.getUniqueId());

        // Ability cooldowns/streaks reset on ANY death, PVP or not.
        victimData.resetAbilityCooldowns();
        victimData.setRangerShotStreak(0);

        Player killer = victim.getKiller();
        if (killer == null) {
            dataManager.save(victimData);
            return; // natural cause - no Rank Up/level economy effects
        }

        // Fresh kill-reward Rank Up, only if the victim actually had a level to lose.
        if (victimData.getPlayerClass() != null && victimData.getLevel() >= 1) {
            event.getDrops().add(CustomItems.rankUp());
        }

        // The victim's own held Rank Ups decrease by 1, if they have any.
        removeOneRankUp(victim);

        // PVP-death counter: tick up, and at 2, drop a level and reset.
        int deaths = victimData.getPvpDeaths() + 1;
        if (deaths >= 2) {
            victimData.addLevel(-1);
            victimData.setPvpDeaths(0);
            victim.sendMessage(Component.text(
                    "You were killed by " + killer.getName() + " (2/2 deaths) - your level dropped!",
                    NamedTextColor.RED));
        } else {
            victimData.setPvpDeaths(deaths);
            victim.sendMessage(Component.text(
                    "You were killed by " + killer.getName() + " (" + deaths + "/2 deaths)",
                    NamedTextColor.RED));
        }
        dataManager.save(victimData);
    }

    private void removeOneRankUp(Player victim) {
        PlayerInventory inv = victim.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (CustomItems.isRankUp(item)) {
                item.setAmount(item.getAmount() - 1);
                inv.setItem(i, item.getAmount() <= 0 ? null : item);
                return;
            }
        }
    }
}
