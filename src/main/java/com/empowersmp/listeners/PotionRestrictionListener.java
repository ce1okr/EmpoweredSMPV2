package com.empowersmp.listeners;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.PlayerClass;
import com.empowersmp.data.DataManager;
import com.empowersmp.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.Set;

/**
 * Restricts specific drinkable potions to the one class they belong to, and
 * bans Turtle Master potions outright for everyone:
 *   - Invisibility potions: Invisibility class only
 *   - Strength II potions: Strength class only
 *   - Speed II potions: Mobility class only
 *   - Fire Resistance potions: Elemental class only
 *   - Turtle Master (all 3 tiers): banned for everyone, no exception
 *
 * This covers the drinkable ITEM. The underlying EFFECT (however it's
 * applied - splash potion thrown by someone else, beacon, /effect give,
 * etc) is separately blocked by ExclusiveEffectListener, since a player
 * could otherwise dodge this restriction entirely by having someone else
 * apply the effect to them.
 *
 * Blocks drinking immediately (PlayerItemConsumeEvent), and also sweeps them
 * out of inventory entirely the same way NetheriteBanListener does, so a
 * disallowed potion can't just sit there waiting to be re-tried.
 */
public class PotionRestrictionListener implements Listener {

    private static final Set<PotionType> INVISIBILITY_TYPES = Set.of(
            PotionType.INVISIBILITY, PotionType.LONG_INVISIBILITY);
    private static final Set<PotionType> STRENGTH_2_TYPES = Set.of(PotionType.STRONG_STRENGTH);
    private static final Set<PotionType> SPEED_2_TYPES = Set.of(PotionType.STRONG_SWIFTNESS);
    private static final Set<PotionType> FIRE_RESISTANCE_TYPES = Set.of(
            PotionType.FIRE_RESISTANCE, PotionType.LONG_FIRE_RESISTANCE);
    private static final Set<PotionType> TURTLE_MASTER_TYPES = Set.of(
            PotionType.TURTLE_MASTER, PotionType.LONG_TURTLE_MASTER, PotionType.STRONG_TURTLE_MASTER);

    private final EmpowerSMP plugin;
    private final DataManager dataManager;

    public PotionRestrictionListener(EmpowerSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (isBanned(player, event.getItem())) {
            event.setCancelled(true);
            removeMatchingFromHand(player, event.getItem());
            player.sendMessage(Component.text(
                    "That potion isn't allowed for your class.", NamedTextColor.RED));
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
            ItemStack item = inv.getItem(i);
            if (isBanned(player, item)) {
                inv.setItem(i, null);
                removedAny = true;
            }
        }
        ItemStack offhand = inv.getItemInOffHand();
        if (isBanned(player, offhand)) {
            inv.setItemInOffHand(null);
            removedAny = true;
        }

        if (removedAny) {
            player.sendMessage(Component.text(
                    "A potion not allowed for your class was removed.", NamedTextColor.RED));
        }
    }

    private void removeMatchingFromHand(Player player, ItemStack consumed) {
        for (org.bukkit.inventory.EquipmentSlot slot : new org.bukkit.inventory.EquipmentSlot[]{
                org.bukkit.inventory.EquipmentSlot.HAND, org.bukkit.inventory.EquipmentSlot.OFF_HAND}) {
            ItemStack held = player.getInventory().getItem(slot);
            if (held != null && held.isSimilar(consumed)) {
                held.setAmount(held.getAmount() - 1);
                player.getInventory().setItem(slot, held.getAmount() <= 0 ? null : held);
                return;
            }
        }
    }

    private boolean isBanned(Player player, ItemStack item) {
        PotionType type = typeOf(item);
        if (type == null) return false;

        if (TURTLE_MASTER_TYPES.contains(type)) return true;

        PlayerData data = dataManager.get(player.getUniqueId());
        PlayerClass playerClass = data.getPlayerClass();

        if (INVISIBILITY_TYPES.contains(type)) return playerClass != PlayerClass.INVISIBILITY;
        if (STRENGTH_2_TYPES.contains(type)) return playerClass != PlayerClass.STRENGTH;
        if (SPEED_2_TYPES.contains(type)) return playerClass != PlayerClass.MOBILITY;
        if (FIRE_RESISTANCE_TYPES.contains(type)) return playerClass != PlayerClass.ELEMENTAL;

        return false;
    }

    private PotionType typeOf(ItemStack item) {
        if (item == null) return null;
        if (!(item.getItemMeta() instanceof PotionMeta meta)) return null;
        return meta.getBasePotionType();
    }
}
