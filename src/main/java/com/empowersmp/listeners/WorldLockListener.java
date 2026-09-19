package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.PlayerClass;
import com.empowersmp.data.DataManager;
import com.empowersmp.data.PlayerData;
import com.empowersmp.world.WorldLockManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Enforces the three server-wide locks: Nether portals, End portals (and End
 * gateways), and villager/wandering-trader interaction. Elemental-class
 * players always get early Nether access regardless of the lock; everyone
 * else (and every class for End/Villagers) is blocked until the matching
 * /empower unlock command has been run.
 */
public class WorldLockListener implements Listener {

    private final WorldLockManager locks;
    private final DataManager dataManager;

    public WorldLockListener(EmpowerSMP plugin) {
        this.locks = plugin.getWorldLockManager();
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        Player player = event.getPlayer();

        if (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && !locks.isNetherUnlocked()) {
            PlayerData data = dataManager.get(player.getUniqueId());
            if (data.getPlayerClass() == PlayerClass.ELEMENTAL) {
                return; // Elemental gets early access, lock or no lock
            }
            event.setCancelled(true);
            player.sendMessage(Component.text("The Nether is locked. An admin needs to unlock it first.", NamedTextColor.RED));
            return;
        }

        if ((cause == PlayerTeleportEvent.TeleportCause.END_PORTAL
                || cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY)
                && !locks.isEndUnlocked()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("The End is locked. An admin needs to unlock it first.", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (locks.isVillagersUnlocked()) return;
        EntityType type = event.getRightClicked().getType();
        if (type == EntityType.VILLAGER || type == EntityType.WANDERING_TRADER) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text(
                    "Villagers are locked. An admin needs to unlock them first.", NamedTextColor.RED));
        }
    }
}
