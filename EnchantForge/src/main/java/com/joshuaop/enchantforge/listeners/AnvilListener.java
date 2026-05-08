package com.joshuaop.enchantforge.listeners;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.utils.ColorUtils;
import com.joshuaop.enchantforge.utils.EnchantUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AnvilListener implements Listener {

    private final EnchantForge plugin;

    public AnvilListener(EnchantForge plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack base = anvil.getItem(0);
        ItemStack sacrifice = anvil.getItem(1);

        if (base == null || sacrifice == null) return;

        // Only process if either item has custom enchants
        Map<String, Integer> sacrificeEnchants = EnchantUtils.getEnchants(sacrifice);
        if (sacrificeEnchants.isEmpty()) return;

        // Merge custom enchants
        ItemStack result = plugin.getAnvilManager().mergeEnchants(base, sacrifice);
        if (result == null) return;

        // Set the result (Bedrock-safe: use setResult)
        event.setResult(result);

        // Add extra XP cost
        int extraCost = plugin.getAnvilManager().calculateMergeCost(base, sacrifice);
        if (extraCost > 0) {
            anvil.setRepairCost(anvil.getRepairCost() + extraCost);
        }
    }
}
