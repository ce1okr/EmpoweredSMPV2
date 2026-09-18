package com.empowersmp.classes;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Grants each class's one-time "Level 0" starting kit the moment a player
 * picks that class. Gear-based kits (sword, armor, spear, bow, potions,
 * spawn eggs) are handed out here; world-access kits (Elemental's early
 * Nether access, Prosperity's villager access) are enforced by the
 * world-restriction listeners added in Phase 2, not by giving an item here.
 */
public final class StartingKits {

    private StartingKits() {}

    public static void grant(Player player, PlayerClass playerClass) {
        switch (playerClass) {
            case STRENGTH -> player.getInventory().addItem(strengthSword());
            case DEFENSE -> {
                player.getInventory().addItem(defenseChestplate());
                player.getInventory().addItem(defenseLeggings());
            }
            case MOBILITY -> player.getInventory().addItem(mobilitySpear());
            case RANGER -> {
                player.getInventory().addItem(rangerBow());
                player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            }
            case VITALITY -> applyVitalityHeartBoost(player);
            case ELEMENTAL -> { /* Nether access is enforced by the world-restriction listener, not an item */ }
            case INVISIBILITY -> {
                player.getInventory().addItem(invisibilityPotion());
                player.getInventory().addItem(invisibilityPotion());
            }
            case PROSPERITY -> player.getInventory().addItem(new ItemStack(Material.VILLAGER_SPAWN_EGG, 2));
        }
    }

    private static ItemStack strengthSword() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, 4);
        return item;
    }

    private static ItemStack defenseChestplate() {
        ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
        item.addUnsafeEnchantment(Enchantment.PROTECTION, 3);
        return item;
    }

    private static ItemStack defenseLeggings() {
        ItemStack item = new ItemStack(Material.DIAMOND_LEGGINGS);
        item.addUnsafeEnchantment(Enchantment.PROTECTION, 3);
        return item;
    }

    private static ItemStack mobilitySpear() {
        ItemStack item = new ItemStack(Material.IRON_SPEAR);
        item.addUnsafeEnchantment(Enchantment.LUNGE, 3);
        return item;
    }

    private static ItemStack rangerBow() {
        ItemStack item = new ItemStack(Material.BOW);
        item.addUnsafeEnchantment(Enchantment.POWER, 4);
        return item;
    }

    private static ItemStack invisibilityPotion() {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 8 * 60 * 20, 0), true);
        item.setItemMeta(meta);
        return item;
    }

    private static void applyVitalityHeartBoost(Player player) {
        AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);
        if (health != null && health.getBaseValue() < 22.0) {
            double old = health.getBaseValue();
            health.setBaseValue(22.0); // 11 hearts
            player.setHealth(Math.min(22.0, player.getHealth() + (22.0 - old)));
        }
    }
}
