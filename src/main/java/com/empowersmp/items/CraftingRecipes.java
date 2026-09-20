package com.empowersmp.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Registers the Level Upgrader crafting recipe:
 *   D D D
 *   R N R
 *   D D D
 * D = Diamond Block, R = Rank Up, N = Nether Star.
 */
public final class CraftingRecipes {

    private CraftingRecipes() {}

    public static void registerAll(JavaPlugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "level_upgrader");
        ShapedRecipe recipe = new ShapedRecipe(key, CustomItems.levelUpgrader());
        recipe.shape("DDD", "RNR", "DDD");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('N', Material.NETHER_STAR);
        recipe.setIngredient('R', new RecipeChoice.ExactChoice(List.of(CustomItems.rankUp())));
        plugin.getServer().addRecipe(recipe);
    }
}
