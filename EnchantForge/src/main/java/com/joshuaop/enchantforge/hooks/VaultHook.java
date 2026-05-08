package com.joshuaop.enchantforge.hooks;

import com.joshuaop.enchantforge.EnchantForge;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault economy integration.
 * Used by ECONOMY effect type in YAML enchants.
 */
public class VaultHook {

    private final EnchantForge plugin;
    private Economy economy = null;
    private boolean enabled = false;

    public VaultHook(EnchantForge plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getConfigManager().getMainConfig().getBoolean("hooks.vault.enabled", true)) {
            return;
        }
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found — economy features disabled.");
            return;
        }
        try {
            RegisteredServiceProvider<Economy> rsp =
                    plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                enabled = true;
                plugin.getLogger().info("Vault economy hooked: " + economy.getName());
            }
        } catch (Exception | NoClassDefFoundError e) {
            plugin.getLogger().warning("Failed to hook Vault: " + e.getMessage());
        }
    }

    public void deposit(Player player, double amount) {
        if (!enabled || economy == null) return;
        economy.depositPlayer(player, amount);
    }

    public void withdraw(Player player, double amount) {
        if (!enabled || economy == null) return;
        economy.withdrawPlayer(player, amount);
    }

    public double getBalance(Player player) {
        if (!enabled || economy == null) return 0;
        return economy.getBalance(player);
    }

    public boolean has(Player player, double amount) {
        if (!enabled || economy == null) return false;
        return economy.has(player, amount);
    }

    public boolean isEnabled() { return enabled; }
    public Economy getEconomy() { return economy; }
}
