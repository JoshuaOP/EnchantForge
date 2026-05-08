package com.joshuaop.enchantforge.utils;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.enchants.CustomEnchant;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class EnchantUtils {

    public static final NamespacedKey ENCHANT_KEY =
            new NamespacedKey(EnchantForge.getInstance(), "enchants");
    public static final NamespacedKey ENCHANT_BOOK_KEY =
            new NamespacedKey(EnchantForge.getInstance(), "enchant_book");
    public static final NamespacedKey SCROLL_KEY =
            new NamespacedKey(EnchantForge.getInstance(), "scroll_id");
    public static final NamespacedKey WHITE_SCROLL_KEY =
            new NamespacedKey(EnchantForge.getInstance(), "white_scroll");

    private static final Random RANDOM = new Random();

    private EnchantUtils() {}

    /**
     * Read all custom enchants stored on an item's PDC.
     * Format: "enchantId:level,enchantId:level,..."
     */
    public static Map<String, Integer> getEnchants(ItemStack item) {
        Map<String, Integer> result = new HashMap<>();
        if (item == null || !item.hasItemMeta()) return result;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String raw = pdc.get(ENCHANT_KEY, PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return result;
        for (String entry : raw.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    result.put(parts[0], Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {}
            }
        }
        return result;
    }

    /**
     * Write the enchant map back to the item's PDC.
     */
    public static ItemStack setEnchants(ItemStack item, Map<String, Integer> enchants) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (enchants.isEmpty()) {
            meta.getPersistentDataContainer().remove(ENCHANT_KEY);
        } else {
            StringBuilder sb = new StringBuilder();
            enchants.forEach((id, lvl) -> sb.append(id).append(":").append(lvl).append(","));
            if (!sb.isEmpty()) sb.setLength(sb.length() - 1);
            meta.getPersistentDataContainer().set(ENCHANT_KEY, PersistentDataType.STRING, sb.toString());
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Add or upgrade a custom enchant on an item.
     */
    public static ItemStack addEnchant(ItemStack item, CustomEnchant enchant, int level) {
        Map<String, Integer> enchants = getEnchants(item);
        enchants.put(enchant.getId(), level);
        item = setEnchants(item, enchants);
        return updateLore(item);
    }

    /**
     * Remove a custom enchant from an item.
     */
    public static ItemStack removeEnchant(ItemStack item, String enchantId) {
        Map<String, Integer> enchants = getEnchants(item);
        enchants.remove(enchantId);
        item = setEnchants(item, enchants);
        return updateLore(item);
    }

    /**
     * Check if an item has a specific custom enchant.
     */
    public static boolean hasEnchant(ItemStack item, String enchantId) {
        return getEnchants(item).containsKey(enchantId);
    }

    /**
     * Get the level of a specific custom enchant on an item.
     */
    public static int getEnchantLevel(ItemStack item, String enchantId) {
        return getEnchants(item).getOrDefault(enchantId, 0);
    }

    /**
     * Rebuild the custom enchant lore section of an item.
     */
    public static ItemStack updateLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return item;
        var manager = EnchantForge.getInstance().getEnchantManager();
        var configManager = EnchantForge.getInstance().getConfigManager();
        Map<String, Integer> enchants = getEnchants(item);
        ItemMeta meta = item.getItemMeta();

        // Strip existing enchant lore lines (marked with our prefix marker)
        List<Component> existingLore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        existingLore.removeIf(line -> {
            String plain = ColorUtils.strip(EnchantForge.mm().serialize(line));
            // Remove lines that match enchant display patterns
            return plain.startsWith("§") || manager.isEnchantLoreLine(line);
        });

        // Build new enchant lore
        String fmt = configManager.getMainConfig().getString("enchants.lore-format", "<gray>{name} {roman}");
        List<Component> enchantLines = new ArrayList<>();
        enchants.forEach((id, level) -> {
            CustomEnchant ce = manager.getEnchant(id);
            if (ce == null) return;
            String display = fmt
                    .replace("{name}", ce.getDisplayName())
                    .replace("{level}", String.valueOf(level))
                    .replace("{roman}", ColorUtils.toRoman(level))
                    .replace("{description}", ce.getDescription());
            enchantLines.add(ColorUtils.parse(display));
        });

        // Prepend enchant lines before existing lore
        enchantLines.addAll(existingLore);
        meta.lore(enchantLines);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Mark an item with a white scroll (destroy protection).
     */
    public static ItemStack applyWhiteScroll(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.getPersistentDataContainer().set(WHITE_SCROLL_KEY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean hasWhiteScroll(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(WHITE_SCROLL_KEY, PersistentDataType.BOOLEAN, false);
    }

    public static boolean isEnchantBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(ENCHANT_BOOK_KEY, PersistentDataType.STRING);
    }

    public static String getBookEnchantId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(ENCHANT_BOOK_KEY, PersistentDataType.STRING);
    }

    public static boolean chance(double percent) {
        return RANDOM.nextDouble() * 100 < percent;
    }

    public static boolean isTool(Material material) {
        String name = material.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE") ||
               name.endsWith("_SHOVEL") || name.endsWith("_HOE") ||
               name.endsWith("_SWORD") || material == Material.FISHING_ROD ||
               material == Material.BOW || material == Material.CROSSBOW ||
               material == Material.TRIDENT || material == Material.ELYTRA;
    }

    public static boolean isArmor(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
               name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }
}
