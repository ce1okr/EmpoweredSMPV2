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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Right-clicking a Level Upgrader permanently raises the holder's own class
 * level by 1 (consuming one Level Upgrader). Does nothing - and isn't
 * consumed - if the player hasn't been assigned a class yet, or is already
 * at max level for their class.
 */
public class LevelUpgraderListener implements Listener {

    private final DataManager dataManager;

    public LevelUpgraderListener(EmpowerSMP plugin) {
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!CustomItems.isLevelUpgrader(item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        PlayerData data = dataManager.get(player.getUniqueId());

        if (data.getPlayerClass() == null) {
            player.sendMessage(Component.text("You need a class before you can use this.", NamedTextColor.RED));
            return;
        }
        if (data.getLevel() >= data.getPlayerClass().maxLevel()) {
            player.sendMessage(Component.text("You're already at max level for your class.", NamedTextColor.RED));
            return;
        }

        data.addLevel(1);
        dataManager.save(data);
        item.setAmount(item.getAmount() - 1);
        player.sendMessage(Component.text(
                "Your " + data.getPlayerClass().displayName() + " level is now " + data.getLevel() + "!",
                NamedTextColor.LIGHT_PURPLE));
    }
}
