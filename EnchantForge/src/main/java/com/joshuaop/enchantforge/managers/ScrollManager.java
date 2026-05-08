package com.joshuaop.enchantforge.managers;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.utils.ColorUtils;
import com.joshuaop.enchantforge.utils.EnchantUtils;
import com.joshuaop.enchantforge.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ScrollManager {

    private final EnchantForge plugin;

    public ScrollManager(EnchantForge plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        // Scrolls are config-driven, nothing to clear from memory
    }

    public ItemStack createScroll(String scrollId) {
        ConfigurationSection section = plugin.getConfigManager()
                .getScrollsConfig()
                .getConfigurationSection("scrolls." + scrollId);
        if (section == null) return null;

        String matName = section.getString("material", "PAPER");
        Material material;
        try {
            material = Material.valueOf(matName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.PAPER;
        }

        String display = section.getString("display", scrollId);
        List<String> description = section.getStringList("description");
        boolean glow = section.getBoolean("glow", true);
        int cmdData = section.getInt("custom-model-data", 0);

        var builder = new ItemBuilder(material)
                .name(display)
                .lore(description, true)
                .glow(glow)
                .pdc(EnchantUtils.SCROLL_KEY, PersistentDataType.STRING, scrollId);

        if (cmdData > 0) builder.customModelData(cmdData);

        return builder.build();
    }

    public boolean isScroll(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(EnchantUtils.SCROLL_KEY, PersistentDataType.STRING);
    }

    public String getScrollId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(EnchantUtils.SCROLL_KEY, PersistentDataType.STRING);
    }

    /** Apply a white scroll — marks item as protected. */
    public boolean applyWhiteScroll(ItemStack target) {
        if (target == null || target.getType() == Material.AIR) return false;
        EnchantUtils.applyWhiteScroll(target);
        return true;
    }

    /** Apply a black scroll — removes a random enchant from the item. */
    public boolean applyBlackScroll(ItemStack target) {
        var enchants = EnchantUtils.getEnchants(target);
        if (enchants.isEmpty()) return false;
        String randomId = enchants.keySet().stream()
                .skip((long) (Math.random() * enchants.size()))
                .findFirst().orElse(null);
        if (randomId == null) return false;
        EnchantUtils.removeEnchant(target, randomId);
        return true;
    }

    /** Apply an upgrade scroll — upgrades a random enchant by 1 level. */
    public boolean applyUpgradeScroll(ItemStack target) {
        var enchants = EnchantUtils.getEnchants(target);
        if (enchants.isEmpty()) return false;
        var manager = plugin.getEnchantManager();

        String chosen = enchants.keySet().stream()
                .filter(id -> {
                    var ce = manager.getEnchant(id);
                    return ce != null && enchants.get(id) < ce.getMaxLevel();
                })
                .skip((long) (Math.random() * enchants.size()))
                .findFirst().orElse(null);
        if (chosen == null) return false;

        int newLevel = enchants.get(chosen) + 1;
        enchants.put(chosen, newLevel);
        EnchantUtils.setEnchants(target, enchants);
        EnchantUtils.updateLore(target);
        return true;
    }

    /** Apply a transmog scroll — re-rolls all enchants (removes all). */
    public boolean applyTransmogScroll(ItemStack target) {
        EnchantUtils.setEnchants(target, new java.util.HashMap<>());
        EnchantUtils.updateLore(target);
        return true;
    }
}
