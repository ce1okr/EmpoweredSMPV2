package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.PlayerClass;
import com.empowersmp.data.DataManager;
import com.empowersmp.data.PlayerData;
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
 * PHASE 2: netherite is split into 3 exclusivity groups, each locked to one
 * class:
 *   - Armor (helmet/chest/legs/boots): Defense class, level 3+ ("Can Use
 *     Netherite Armor")
 *   - Tools (pickaxe/axe/shovel/hoe):  Prosperity class, level 3+ ("Ability
 *     to Use Netherite Tools")
 *   - Sword:                           Strength class, ANY level - no
 *     specific level was given for this rule when it was added, so it's
 *     treated as a permanent class perk rather than a level unlock. Flag
 *     this if a level gate was actually intended.
 *
 * Anyone else - including a killer who loots a qualifying player's gear off
 * their corpse - has it stripped out via the same sweep pattern used
 * elsewhere. Smithing-table upgrades into netherite are blocked the same way
 * unless the crafter currently qualifies for that specific piece.
 */
public class NetheriteBanListener implements Listener {

    private static final Set<Material> ARMOR = Set.of(
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS);
    private static final Set<Material> TOOLS = Set.of(
            Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE,
            Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE);
    private static final Material SWORD = Material.NETHERITE_SWORD;

    private final EmpowerSMP plugin;
    private final DataManager dataManager;

    public NetheriteBanListener(EmpowerSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    private boolean isAllowed(Player player, Material type) {
        PlayerData data = dataManager.get(player.getUniqueId());
        PlayerClass pc = data.getPlayerClass();
        int level = data.getLevel();
        if (ARMOR.contains(type)) return pc == PlayerClass.DEFENSE && level >= 3;
        if (TOOLS.contains(type)) return pc == PlayerClass.PROSPERITY && level >= 3;
        if (type == SWORD) return pc == PlayerClass.STRENGTH;
        return true; // not netherite gear we govern
    }

    private boolean isGoverned(Material type) {
        return ARMOR.contains(type) || TOOLS.contains(type) || type == SWORD;
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        ItemStack result = event.getResult();
        if (result == null) return;
        if (!isGoverned(result.getType())) return;
        if (!(event.getView().getPlayer() instanceof Player player)) return;
        if (!isAllowed(player, result.getType())) {
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
            if (removeIfDisallowed(player, inv, i)) removedAny = true;
        }
        ItemStack offhand = inv.getItemInOffHand();
        if (offhand != null && isGoverned(offhand.getType()) && !isAllowed(player, offhand.getType())) {
            inv.setItemInOffHand(null);
            removedAny = true;
        }
        if (removedAny) {
            player.sendMessage(Component.text(
                    "That netherite item isn't allowed for your class/level - it was removed.",
                    NamedTextColor.RED));
        }
    }

    private boolean removeIfDisallowed(Player player, PlayerInventory inv, int slot) {
        ItemStack item = inv.getItem(slot);
        if (item != null && isGoverned(item.getType()) && !isAllowed(player, item.getType())) {
            inv.setItem(slot, null);
            return true;
        }
        return false;
    }
}
