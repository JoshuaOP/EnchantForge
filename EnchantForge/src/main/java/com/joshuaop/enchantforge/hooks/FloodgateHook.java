package com.joshuaop.enchantforge.hooks;

import com.joshuaop.enchantforge.EnchantForge;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Floodgate integration for detecting Bedrock players.
 * Gracefully degrades if Floodgate is not installed.
 */
public class FloodgateHook {

    private final EnchantForge plugin;
    private boolean enabled = false;
    private Object floodgateApi = null;

    public FloodgateHook(EnchantForge plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getConfigManager().getMainConfig().getBoolean("hooks.floodgate.enabled", true)) {
            return;
        }
        if (plugin.getServer().getPluginManager().getPlugin("floodgate") == null) {
            plugin.getLogger().info("Floodgate not found — Bedrock detection disabled.");
            return;
        }
        try {
            floodgateApi = org.geysermc.floodgate.api.FloodgateApi.getInstance();
            enabled = floodgateApi != null;
            if (enabled) plugin.getLogger().info("Floodgate hooked successfully.");
        } catch (Exception | NoClassDefFoundError e) {
            plugin.getLogger().warning("Failed to hook Floodgate: " + e.getMessage());
        }
    }

    /**
     * Returns true if the given player is a Bedrock (Floodgate) player.
     */
    public boolean isBedrockPlayer(Player player) {
        return isBedrockPlayer(player.getUniqueId());
    }

    public boolean isBedrockPlayer(UUID uuid) {
        if (!enabled || floodgateApi == null) return false;
        try {
            return ((org.geysermc.floodgate.api.FloodgateApi) floodgateApi).isFloodgatePlayer(uuid);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEnabled() { return enabled; }
}
