package com.joshuaop.enchantforge.managers;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.utils.EnchantUtils;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Handles combining two enchanted items (or book + item) in the anvil,
 * merging custom enchants from the sacrifice into the base.
 */
public class AnvilManager {

    private final EnchantForge plugin;

    public AnvilManager(EnchantForge plugin) {
        this.plugin = plugin;
    }

    /**
     * Merge custom enchants from sacrifice into base.
     *
     * @return modified base ItemStack, or null if no custom enchants to merge
     */
    public ItemStack mergeEnchants(ItemStack base, ItemStack sacrifice) {
        if (base == null || sacrifice == null) return null;

        Map<String, Integer> baseEnchants = EnchantUtils.getEnchants(base);
        Map<String, Integer> sacrificeEnchants = EnchantUtils.getEnchants(sacrifice);

        if (sacrificeEnchants.isEmpty()) return null;

        boolean changed = false;
        var enchantManager = plugin.getEnchantManager();

        for (Map.Entry<String, Integer> entry : sacrificeEnchants.entrySet()) {
            String id = entry.getKey();
            int sacrificeLevel = entry.getValue();
            CustomEnchant enchant = enchantManager.getEnchant(id);
            if (enchant == null) continue;

            // Check compatibility with base item
            if (!enchant.canApply(base)) continue;

            int baseLevel = baseEnchants.getOrDefault(id, 0);
            int resultLevel;

            if (baseLevel == sacrificeLevel && baseLevel < enchant.getMaxLevel()) {
                // Same level: combine up
                resultLevel = Math.min(baseLevel + 1, enchant.getMaxLevel());
            } else if (sacrificeLevel > baseLevel) {
                // Higher level: take the higher one
                resultLevel = Math.min(sacrificeLevel, enchant.getMaxLevel());
            } else {
                continue; // base already has equal or higher level
            }

            // Check max enchants per item cap
            int maxPerItem = plugin.getConfigManager().getMainConfig()
                    .getInt("enchants.max-per-item", 10);
            if (!baseEnchants.containsKey(id) && baseEnchants.size() >= maxPerItem) continue;

            baseEnchants.put(id, resultLevel);
            changed = true;
        }

        if (!changed) return null;

        ItemStack result = base.clone();
        EnchantUtils.setEnchants(result, baseEnchants);
        EnchantUtils.updateLore(result);
        return result;
    }

    /**
     * Calculate the extra XP cost for merging custom enchants.
     */
    public int calculateMergeCost(ItemStack base, ItemStack sacrifice) {
        Map<String, Integer> sacrificeEnchants = EnchantUtils.getEnchants(sacrifice);
        int costPerEnchant = plugin.getConfigManager().getMainConfig()
                .getInt("combining.xp-cost-per-enchant", 5);
        return sacrificeEnchants.size() * costPerEnchant;
    }
}
