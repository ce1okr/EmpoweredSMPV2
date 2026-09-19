package com.empowersmp.commands;

import com.empowersmp.EmpowerSMP;
import com.empowersmp.classes.PlayerClass;
import com.empowersmp.classes.StartingKits;
import com.empowersmp.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * /empower class <name>              - pick your class (once; admin can reset)
 * /empower give <player> <amount>    - grant levels within their current class (admin, after events)
 * /empower set  <player> <level>     - set an exact level within their current class (admin)
 * /empower resetclass <player>       - clear a player's class so they can pick again (admin)
 * /empower unlock <nether|end|villagers> - permanently unlock for everyone (admin)
 * /empower levels [player]           - view current class + level
 * /empower info                      - list the classes
 */
public class EmpowerCommand implements CommandExecutor, TabCompleter {

    private final EmpowerSMP plugin;

    public EmpowerCommand(EmpowerSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /empower <class|give|set|levels|resetclass|unlock|info>", NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "class" -> handlePickClass(sender, args);
            case "give" -> handleGiveOrSet(sender, args, true);
            case "set" -> handleGiveOrSet(sender, args, false);
            case "resetclass" -> handleResetClass(sender, args);
            case "unlock" -> handleUnlock(sender, args);
            case "levels" -> handleLevels(sender, args);
            case "info" -> handleInfo(sender);
            default -> sender.sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
        }
        return true;
    }

    private void handlePickClass(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /empower class <name>", NamedTextColor.RED));
            return;
        }
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data.getPlayerClass() != null) {
            player.sendMessage(Component.text(
                    "You already picked " + data.getPlayerClass().displayName()
                            + ". Ask an admin to reset your class if you need to change.",
                    NamedTextColor.RED));
            return;
        }
        PlayerClass playerClass = PlayerClass.fromString(args[1]);
        if (playerClass == null) {
            player.sendMessage(Component.text("Unknown class: " + args[1], NamedTextColor.RED));
            return;
        }
        data.setPlayerClass(playerClass);
        plugin.getDataManager().save(data);
        StartingKits.grant(player, playerClass);
        data.setReceivedStartingKit(true);
        plugin.getDataManager().save(data);
        player.sendMessage(Component.text(
                "You are now " + playerClass.displayName() + "! Your starting kit has been given.",
                NamedTextColor.LIGHT_PURPLE));
    }

    private void handleGiveOrSet(CommandSender sender, String[] args, boolean relative) {
        if (!sender.hasPermission("empowersmp.admin")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /empower " + args[0] + " <player> <amount>", NamedTextColor.RED));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        PlayerData data = plugin.getDataManager().get(target.getUniqueId());
        if (data.getPlayerClass() == null) {
            sender.sendMessage(Component.text(
                    (target.getName() == null ? args[1] : target.getName()) + " hasn't picked a class yet.",
                    NamedTextColor.RED));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Amount must be a number.", NamedTextColor.RED));
            return;
        }

        int before = data.getLevel();
        if (relative) {
            data.addLevel(amount);
        } else {
            data.setLevel(amount);
        }
        int after = data.getLevel();
        plugin.getDataManager().save(data);

        sender.sendMessage(Component.text(
                (target.getName() == null ? args[1] : target.getName()) + "'s " + data.getPlayerClass().displayName()
                        + " level: " + before + " -> " + after, NamedTextColor.GREEN));

        Player online = target.getPlayer();
        if (online != null) {
            online.sendMessage(Component.text(
                    "Your " + data.getPlayerClass().displayName() + " level is now " + after + "!",
                    NamedTextColor.LIGHT_PURPLE));
        }
    }

    private void handleUnlock(CommandSender sender, String[] args) {
        if (!sender.hasPermission("empowersmp.admin")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /empower unlock <nether|end|villagers>", NamedTextColor.RED));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "nether" -> {
                plugin.getWorldLockManager().unlockNether();
                Bukkit.broadcast(Component.text("The Nether has been unlocked for everyone!", NamedTextColor.GOLD));
            }
            case "end" -> {
                plugin.getWorldLockManager().unlockEnd();
                Bukkit.broadcast(Component.text("The End has been unlocked for everyone!", NamedTextColor.GOLD));
            }
            case "villagers" -> {
                plugin.getWorldLockManager().unlockVillagers();
                Bukkit.broadcast(Component.text("Villagers have been unlocked for everyone!", NamedTextColor.GOLD));
            }
            default -> sender.sendMessage(Component.text("Usage: /empower unlock <nether|end|villagers>", NamedTextColor.RED));
        }
    }

    private void handleResetClass(CommandSender sender, String[] args) {
        if (!sender.hasPermission("empowersmp.admin")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /empower resetclass <player>", NamedTextColor.RED));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        PlayerData data = plugin.getDataManager().get(target.getUniqueId());
        data.setPlayerClass(null);
        plugin.getDataManager().save(data);
        sender.sendMessage(Component.text(
                (target.getName() == null ? args[1] : target.getName()) + "'s class has been reset.",
                NamedTextColor.GREEN));
    }

    private void handleLevels(CommandSender sender, String[] args) {
        OfflinePlayer target;
        if (args.length >= 2) {
            target = Bukkit.getOfflinePlayer(args[1]);
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(Component.text("Specify a player.", NamedTextColor.RED));
            return;
        }
        PlayerData data = plugin.getDataManager().get(target.getUniqueId());
        String name = target.getName() == null ? "Player" : target.getName();
        if (data.getPlayerClass() == null) {
            sender.sendMessage(Component.text(name + " hasn't picked a class yet.", NamedTextColor.AQUA));
            return;
        }
        sender.sendMessage(Component.text(
                name + " is " + data.getPlayerClass().displayName()
                        + ", level " + data.getLevel() + "/" + data.getPlayerClass().maxLevel(),
                NamedTextColor.AQUA));
    }

    private void handleInfo(CommandSender sender) {
        sender.sendMessage(Component.text("=== EmpowerSMP Classes ===", NamedTextColor.GOLD));
        for (PlayerClass c : PlayerClass.values()) {
            sender.sendMessage(Component.text("- " + c.displayName() + " (levels 0-" + c.maxLevel() + ")",
                    NamedTextColor.AQUA));
        }
        sender.sendMessage(Component.text("Pick one with /empower class <name>. Choose carefully!",
                NamedTextColor.GRAY));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("class", "give", "set", "resetclass", "unlock", "levels", "info"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("unlock")) {
            return filter(List.of("nether", "end", "villagers"), args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("class")) {
            return filter(Stream.of(PlayerClass.values()).map(PlayerClass::displayName).collect(Collectors.toList()), args[1]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("set")
                || args[0].equalsIgnoreCase("resetclass") || args[0].equalsIgnoreCase("levels"))) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
        }
        return new ArrayList<>();
    }

    private List<String> filter(List<String> options, String prefix) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
