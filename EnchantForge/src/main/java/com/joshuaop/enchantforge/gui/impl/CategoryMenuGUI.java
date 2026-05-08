package com.joshuaop.enchantforge.gui.impl;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantCategory;
import com.joshuaop.enchantforge.gui.GUI;
import com.joshuaop.enchantforge.gui.GUIItem;
import com.joshuaop.enchantforge.utils.ColorUtils;
import com.joshuaop.enchantforge.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.List;

public class CategoryMenuGUI extends GUI {

    private static final int ROWS = 4;

    private record CategoryEntry(EnchantCategory category, Material icon, int slot) {}

    private static final List<CategoryEntry> ENTRIES = List.of(
            new CategoryEntry(EnchantCategory.SWORD,      Material.DIAMOND_SWORD,     10),
            new CategoryEntry(EnchantCategory.AXE,        Material.DIAMOND_AXE,       11),
            new CategoryEntry(EnchantCategory.PICKAXE,    Material.DIAMOND_PICKAXE,   12),
            new CategoryEntry(EnchantCategory.SHOVEL,     Material.DIAMOND_SHOVEL,    13),
            new CategoryEntry(EnchantCategory.HOE,        Material.DIAMOND_HOE,       14),
            new CategoryEntry(EnchantCategory.HELMET,     Material.DIAMOND_HELMET,    19),
            new CategoryEntry(EnchantCategory.CHESTPLATE, Material.DIAMOND_CHESTPLATE,20),
            new CategoryEntry(EnchantCategory.LEGGINGS,   Material.DIAMOND_LEGGINGS,  21),
            new CategoryEntry(EnchantCategory.BOOTS,      Material.DIAMOND_BOOTS,     22),
            new CategoryEntry(EnchantCategory.BOW,        Material.BOW,               23),
            new CategoryEntry(EnchantCategory.CROSSBOW,   Material.CROSSBOW,          24),
            new CategoryEntry(EnchantCategory.FISHING_ROD,Material.FISHING_ROD,       25),
            new CategoryEntry(EnchantCategory.ELYTRA,     Material.ELYTRA,            28),
            new CategoryEntry(EnchantCategory.TRIDENT,    Material.TRIDENT,           29),
            new CategoryEntry(EnchantCategory.UNIVERSAL,  Material.NETHER_STAR,       31)
    );

    public CategoryMenuGUI(EnchantForge plugin, Player player, boolean bedrock) {
        super(plugin, player, bedrock);
    }

    @Override
    protected void build() {
        String title = plugin.getConfigManager().getMessagesConfig()
                .getString("gui.category-menu-title", "<dark_gray>✦ Categories ✦");
        createInventory(ColorUtils.parse(title), ROWS);
        fillBorder(ROWS);

        for (CategoryEntry entry : ENTRIES) {
            long count = plugin.getEnchantManager().getAllEnchants().stream()
                    .filter(e -> entry.category() == EnchantCategory.UNIVERSAL ||
                                 entry.category().getMaterials().stream()
                                      .anyMatch(e::canApply))
                    .count();

            String display = "<white>" + titleCase(entry.category().name());
            List<String> lore = List.of(
                    "<gray>Enchants: <white>" + count,
                    "",
                    "<yellow>Click to browse"
            );

            var item = new ItemBuilder(entry.icon())
                    .name(display)
                    .lore(lore, true)
                    .build();
            inventory.setItem(entry.slot(), item);
        }

        // Back button
        String closeLabel = plugin.getConfigManager().getMessagesConfig()
                .getString("gui.close", "<red>✖ Close");
        inventory.setItem(ROWS * 9 - 5, new ItemBuilder(Material.BARRIER).name(closeLabel).build());

        fill(ROWS);
    }

    @Override
    public void handleClick(int slot, ClickType click) {
        if (slot == ROWS * 9 - 5) {
            player.closeInventory();
            return;
        }
        for (CategoryEntry entry : ENTRIES) {
            if (entry.slot() == slot) {
                plugin.getGUIManager().openEnchantBrowser(player, 1);
                return;
            }
        }
    }

    private String titleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
