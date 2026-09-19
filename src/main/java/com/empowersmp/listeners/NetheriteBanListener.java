package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Set;

/**
 * PHASE 1: Netherite armor and tools are entirely banned for everyone (no
 * class exception yet - Defense L2's "Can Use Netherite Armor" and
 * Prosperity L3's "Ability to Use Netherite Tools" are Phase 2 features that
 * don't exist yet). Blocks creating netherite gear at a smithing table, and
 * sweeps/removes any netherite armor or tools that end up in a player's
 * possession another way (commands, chests, trades, etc).
 *
 * Netherite SWORDS are NOT banned - the design doc only restricts "armor"
 * and "tools" (pickaxe/axe/shovel/hoe), not weapons.
 */
public class NetheriteBanListener implements Listener {

    private static final Set<Material> BANNED = Set.of(
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE,
            Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE
    );

    private final EmpowerSMP plugin;

    public NetheriteBanListener(EmpowerSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        ItemStack result = event.getResult();
        if (result != null && BANNED.contains(result.getType())) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        Bukkit.getScheduler().runTask(plugin, () -> sweepInventory(player));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Bukkit.getScheduler().runTask(plugin, () -> sweepInventory(player));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> sweepInventory(event.getPlayer()), 5L);
    }

    /** Fallback sweep; see EmpowerSMP#startTasks. */
    public void periodicSweep(Player player) {
        sweepInventory(player);
    }

    private void sweepInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        boolean removedAny = false;

        for (int i = 0; i < inv.getSize(); i++) {
            if (removeIfBanned(inv, i)) removedAny = true;
        }
        ItemStack offhand = inv.getItemInOffHand();
        if (offhand != null && BANNED.contains(offhand.getType())) {
            inv.setItemInOffHand(null);
            removedAny = true;
        }

        if (removedAny) {
            player.sendMessage(Component.text(
                    "Netherite armor and tools are banned in Phase 1 - the item was removed.",
                    NamedTextColor.RED));
        }
    }

    private boolean removeIfBanned(PlayerInventory inv, int slot) {
        ItemStack item = inv.getItem(slot);
        if (item != null && BANNED.contains(item.getType())) {
            inv.setItem(slot, null);
            return true;
        }
        return false;
    }
}
