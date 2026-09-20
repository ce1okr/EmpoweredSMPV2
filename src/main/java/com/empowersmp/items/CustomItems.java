package com.empowersmp.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * The two economy items: Rank Up (a kill-reward drop, and the crafting
 * ingredient for a Level Upgrader) and Level Upgrader (right-click to
 * permanently raise your own class level by 1).
 */
public final class CustomItems {

    public static final NamespacedKey RANK_UP_KEY = new NamespacedKey("empowersmp", "rank_up");
    public static final NamespacedKey LEVEL_UPGRADER_KEY = new NamespacedKey("empowersmp", "level_upgrader");

    private CustomItems() {}

    public static ItemStack rankUp() {
        ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Rank Up", NamedTextColor.AQUA, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Dropped by defeating another player", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Craft 2 of these with 6 Diamond Blocks", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("and a Nether Star into a Level Upgrader", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(RANK_UP_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isRankUp(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(RANK_UP_KEY, PersistentDataType.BYTE);
    }

    public static ItemStack levelUpgrader() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Level Upgrader", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Right-click to permanently raise", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("your class level by 1", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(LEVEL_UPGRADER_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isLevelUpgrader(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(LEVEL_UPGRADER_KEY, PersistentDataType.BYTE);
    }
}
