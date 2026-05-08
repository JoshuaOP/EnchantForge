package com.joshuaop.enchantforge.listeners;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import com.joshuaop.enchantforge.utils.ColorUtils;
import com.joshuaop.enchantforge.utils.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class PlayerListener implements Listener {

    private final EnchantForge plugin;

    public PlayerListener(EnchantForge plugin) {
        this.plugin = plugin;
    }

    // ── Join / Quit ────────────────────────────────────────────

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Pre-load player data from DB cache on join (async)
        com.joshuaop.enchantforge.utils.SchedulerUtils.async(() ->
                plugin.getDatabaseManager().getPlayerData(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Flush player data to DB on quit
        com.joshuaop.enchantforge.utils.SchedulerUtils.async(() -> {
            plugin.getDatabaseManager().savePlayer(event.getPlayer().getUniqueId());
            plugin.getDatabaseManager().invalidateCache(event.getPlayer().getUniqueId());
        });
    }

    // ── Enchant book application ───────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        // Scroll application
        if (plugin.getScrollManager().isScroll(hand)) {
            event.setCancelled(true);
            handleScrollUse(player, hand);
            return;
        }

        // Enchant book application via right-click on held item
        if (EnchantUtils.isEnchantBook(hand)) {
            event.setCancelled(true);
            handleBookUse(player, hand);
        }
    }

    private void handleBookUse(Player player, ItemStack book) {
        String data = EnchantUtils.getBookEnchantId(book);
        if (data == null) return;
        String[] parts = data.split(":");
        if (parts.length < 2) return;

        String enchantId = parts[0];
        int level;
        try {
            level = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return;
        }

        CustomEnchant enchant = plugin.getEnchantManager().getEnchant(enchantId);
        if (enchant == null) return;

        // Try to apply to the item in off-hand, or open browser if no suitable item
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() == Material.AIR || !enchant.canApply(offhand)) {
            player.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("enchants.incompatible")));
            return;
        }

        // Check max enchants cap
        int maxPerItem = plugin.getConfigManager().getMainConfig()
                .getInt("enchants.max-per-item", 10);
        var existing = EnchantUtils.getEnchants(offhand);
        if (!existing.containsKey(enchantId) && existing.size() >= maxPerItem) {
            player.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("enchants.max-enchants")
                          .replace("{max}", String.valueOf(maxPerItem))));
            return;
        }

        // Apply
        EnchantUtils.addEnchant(offhand, enchant, level);
        player.sendMessage(ColorUtils.parse(
                plugin.getConfigManager().getMessage("enchants.apply-success")
                      .replace("{enchant}", enchant.getDisplayName())
                      .replace("{level}", String.valueOf(level))));

        // Consume one book
        if (book.getAmount() > 1) {
            book.setAmount(book.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }

    private void handleScrollUse(Player player, ItemStack scroll) {
        String scrollId = plugin.getScrollManager().getScrollId(scroll);
        if (scrollId == null) return;

        // Target the item in off-hand
        ItemStack target = player.getInventory().getItemInOffHand();
        if (target == null || target.getType() == Material.AIR) {
            player.sendMessage(ColorUtils.parse(
                    "<red>Hold the target item in your off-hand."));
            return;
        }

        boolean success = switch (scrollId) {
            case "white_scroll"   -> plugin.getScrollManager().applyWhiteScroll(target);
            case "black_scroll"   -> plugin.getScrollManager().applyBlackScroll(target);
            case "upgrade_scroll" -> plugin.getScrollManager().applyUpgradeScroll(target);
            case "transmog_scroll"-> plugin.getScrollManager().applyTransmogScroll(target);
            default -> false;
        };

        if (success) {
            player.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("enchants.scroll-success")));
            // Consume scroll
            if (scroll.getAmount() > 1) scroll.setAmount(scroll.getAmount() - 1);
            else player.getInventory().setItemInMainHand(null);
        } else {
            player.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("enchants.scroll-failed")));
        }
    }

    // ── ARMOR_EQUIP trigger ────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != player.getInventory()) return;

        int slot = event.getSlot();
        boolean isArmorSlot = slot >= 36 && slot <= 39;
        if (!isArmorSlot) return;

        ItemStack item = event.getCursor();
        if (item == null || item.getType() == Material.AIR) return;

        var enchants = EnchantUtils.getEnchants(item);
        if (enchants.isEmpty()) return;

        com.joshuaop.enchantforge.utils.SchedulerUtils.syncDelayed(() -> {
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                CustomEnchant ce = plugin.getEnchantManager().getEnchant(entry.getKey());
                if (ce == null || ce.getTrigger() != EnchantTrigger.ARMOR_EQUIP) continue;
                ce.apply(player, entry.getValue(), null);
            }
        }, 1L);
    }
}
