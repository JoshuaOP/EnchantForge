package com.joshuaop.enchantforge.hooks;

import com.joshuaop.enchantforge.EnchantForge;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI expansion for EnchantForge.
 *
 * Available placeholders:
 *   %enchantforge_total%           — total registered enchants
 *   %enchantforge_enchants_<item>% — number of custom enchants on held item
 */
public class PlaceholderAPIHook {

    private final EnchantForge plugin;
    private boolean enabled = false;

    public PlaceholderAPIHook(EnchantForge plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getConfigManager().getMainConfig()
                .getBoolean("hooks.placeholderapi.enabled", true)) return;

        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getLogger().info("PlaceholderAPI not found — placeholders disabled.");
            return;
        }

        try {
            new EnchantForgePlaceholders(plugin).register();
            enabled = true;
            plugin.getLogger().info("PlaceholderAPI expansion registered.");
        } catch (Exception | NoClassDefFoundError e) {
            plugin.getLogger().warning("Failed to register PAPI expansion: " + e.getMessage());
        }
    }

    public boolean isEnabled() { return enabled; }

    // ── Inner expansion class ──────────────────────────────────

    public static class EnchantForgePlaceholders extends PlaceholderExpansion {

        private final EnchantForge plugin;

        public EnchantForgePlaceholders(EnchantForge plugin) {
            this.plugin = plugin;
        }

        @Override
        public @NotNull String getIdentifier() { return "enchantforge"; }

        @Override
        public @NotNull String getAuthor() { return "EnchantForge"; }

        @Override
        public @NotNull String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public boolean persist() { return true; }

        @Override
        public String onPlaceholderRequest(Player player, @NotNull String identifier) {
            return switch (identifier) {
                case "total" -> String.valueOf(plugin.getEnchantManager().getEnchantCount());
                case "enchants_held" -> {
                    if (player == null) yield "0";
                    var item = player.getInventory().getItemInMainHand();
                    yield String.valueOf(com.joshuaop.enchantforge.utils.EnchantUtils.getEnchants(item).size());
                }
                default -> null;
            };
        }
    }
}
