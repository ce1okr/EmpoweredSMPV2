package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.PlayerClass;
import com.empowersmp.data.DataManager;
import com.empowersmp.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;

/**
 * Floor-enforces (never lowers, only raises to a minimum) the "on any X you
 * have" style abilities - these are guaranteed minimums, not caps:
 *   - Prosperity L1+: Fortune 3 on any pickaxe/axe
 *   - Prosperity L2+: Looting 3 on any sword
 *   - Strength  L1+: Fire Aspect 2 on any sword
 *   - Elemental L2+: Fire Aspect 2 on any sword
 *   - Ranger    L1+: Power 5 on any bow
 *   - Mobility  L1+: Aqua Affinity 1 + Respiration 3 on any helmet worn,
 *     and Depth Strider 3 OR Frost Walker 2 on any boots worn - whichever
 *     bootChoice they picked via /empower bootchoice. Nothing is applied to
 *     boots until a choice is made.
 *
 * Uses the same sweep pattern (join/pickup/inventory-click + periodic) as
 * the other enforcement listeners.
 */
public class MinimumEnchantListener implements Listener {

    private static final Set<Material> PICKAXES_AXES = Set.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE);

    private static final Set<Material> SWORDS = Set.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD);

    private static final Set<Material> BOWS = Set.of(Material.BOW);

    private static final Set<Material> HELMETS = Set.of(
            Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET,
            Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
            Material.TURTLE_HELMET);

    private static final Set<Material> BOOTS = Set.of(
            Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS,
            Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS);

    private final EmpowerSMP plugin;
    private final DataManager dataManager;

    public MinimumEnchantListener(EmpowerSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        Bukkit.getScheduler().runTask(plugin, () -> sweep(player));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Bukkit.getScheduler().runTask(plugin, () -> sweep(player));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> sweep(event.getPlayer()), 5L);
    }

    /** Fallback sweep; see EmpowerSMP#startTasks. */
    public void periodicSweep(Player player) {
        sweep(player);
    }

    public void sweep(Player player) {
        PlayerData data = dataManager.get(player.getUniqueId());
        PlayerClass pc = data.getPlayerClass();
        if (pc == null) return;
        int level = data.getLevel();

        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            apply(inv.getItem(i), pc, level, data.getBootChoice());
        }
        apply(inv.getItemInOffHand(), pc, level, data.getBootChoice());
    }

    private void apply(ItemStack item, PlayerClass pc, int level, PlayerData.BootChoice bootChoice) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        boolean changed = false;
        Material type = item.getType();

        if (pc == PlayerClass.PROSPERITY && level >= 1 && PICKAXES_AXES.contains(type)) {
            changed |= floor(meta, Enchantment.FORTUNE, 3);
        }
        if (pc == PlayerClass.PROSPERITY && level >= 2 && SWORDS.contains(type)) {
            changed |= floor(meta, Enchantment.LOOTING, 3);
        }
        if (((pc == PlayerClass.STRENGTH && level >= 1) || (pc == PlayerClass.ELEMENTAL && level >= 2))
                && SWORDS.contains(type)) {
            changed |= floor(meta, Enchantment.FIRE_ASPECT, 2);
        }
        if (pc == PlayerClass.RANGER && level >= 1 && BOWS.contains(type)) {
            changed |= floor(meta, Enchantment.POWER, 5);
        }
        if (pc == PlayerClass.MOBILITY && level >= 1 && HELMETS.contains(type)) {
            changed |= floor(meta, Enchantment.AQUA_AFFINITY, 1);
            changed |= floor(meta, Enchantment.RESPIRATION, 3);
        }
        if (pc == PlayerClass.MOBILITY && level >= 1 && BOOTS.contains(type)) {
            if (bootChoice == PlayerData.BootChoice.DEPTH_STRIDER) {
                changed |= floor(meta, Enchantment.DEPTH_STRIDER, 3);
            } else if (bootChoice == PlayerData.BootChoice.FROST_WALKER) {
                changed |= floor(meta, Enchantment.FROST_WALKER, 2);
            }
        }

        if (changed) item.setItemMeta(meta);
    }

    /** Raises `ench` on `meta` to at least `minLevel`, never lowering it. Returns true if changed. */
    private boolean floor(ItemMeta meta, Enchantment ench, int minLevel) {
        int current = meta.getEnchantLevel(ench);
        if (current >= minLevel) return false;
        meta.addEnchant(ench, minLevel, true);
        return true;
    }
}
