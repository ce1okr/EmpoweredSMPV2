package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
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
 * gateways), and villager/wandering-trader interaction. All blocked for
 * everyone until the matching /empower unlock command has been run.
 */
public class WorldLockListener implements Listener {

    private final WorldLockManager locks;

    public WorldLockListener(EmpowerSMP plugin) {
        this.locks = plugin.getWorldLockManager();
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        Player player = event.getPlayer();

        if (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && !locks.isNetherUnlocked()) {
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
