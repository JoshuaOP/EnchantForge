package com.joshuaop.enchantforge.gui.impl;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.gui.GUI;
import com.joshuaop.enchantforge.gui.GUIItem;
import com.joshuaop.enchantforge.utils.ColorUtils;
import com.joshuaop.enchantforge.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EnchantBrowserGUI extends GUI {

    private int page;
    private final List<CustomEnchant> enchantList;

    private static final int ROWS = 6;
    private static final int CONTENT_SLOTS = 45; // slots 0-44
    private static final int PREV_SLOT = 45;
    private static final int PAGE_INFO_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    public EnchantBrowserGUI(EnchantForge plugin, Player player, int page, boolean bedrock) {
        super(plugin, player, bedrock);
        this.page = page;
        this.enchantList = new ArrayList<>(plugin.getEnchantManager().getAllEnchants());
        // Bedrock: reduce to 4 rows for mobile-friendly layout
        if (bedrock) ROWS_OVERRIDE: {
            // Use standard 6-row for now; Bedrock 4-row variant can be added
        }
    }

    @Override
    protected void build() {
        String title = plugin.getConfigManager().getMessagesConfig()
                .getString("gui.enchant-browser-title", "<dark_gray>✦ Enchant Browser ✦");
        createInventory(ColorUtils.parse(title), ROWS);

        int perPage = plugin.getConfigManager().getMainConfig()
                .getInt("gui.enchants-per-page", 45);
        int totalPages = (int) Math.ceil((double) enchantList.size() / perPage);
        int startIndex = (page - 1) * perPage;

        // Populate enchant items
        for (int i = 0; i < perPage && (startIndex + i) < enchantList.size(); i++) {
            CustomEnchant enchant = enchantList.get(startIndex + i);
            var item = buildEnchantItem(enchant);
            inventory.setItem(i, item.getItem());
        }

        // Navigation
        if (page > 1) {
            String prevLabel = plugin.getConfigManager().getMessagesConfig()
                    .getString("gui.prev-page", "<green>« Previous Page");
            var prevItem = new ItemBuilder(Material.ARROW)
                    .name(prevLabel)
                    .build();
            inventory.setItem(PREV_SLOT, prevItem);
        }

        String pageInfo = plugin.getConfigManager().getMessagesConfig()
                .getString("gui.page-info", "<gray>Page <white>{current}</white>/<white>{total}</white>")
                .replace("{current}", String.valueOf(page))
                .replace("{total}", String.valueOf(totalPages));
        inventory.setItem(PAGE_INFO_SLOT, new ItemBuilder(Material.PAPER)
                .name(pageInfo).build());

        if (page < totalPages) {
            String nextLabel = plugin.getConfigManager().getMessagesConfig()
                    .getString("gui.next-page", "<green>Next Page »");
            inventory.setItem(NEXT_SLOT, new ItemBuilder(Material.ARROW)
                    .name(nextLabel).build());
        }

        // Close button
        String closeLabel = plugin.getConfigManager().getMessagesConfig()
                .getString("gui.close", "<red>✖ Close");
        inventory.setItem(45, new ItemBuilder(Material.BARRIER).name(closeLabel).build());

        fill(ROWS);
    }

    private GUIItem buildEnchantItem(CustomEnchant enchant) {
        List<String> loreLines = new ArrayList<>();
        loreLines.add("<dark_gray>" + enchant.getDescription());
        loreLines.add("");
        loreLines.add("<gray>Rarity: <white>" + enchant.getRarity().name());
        loreLines.add("<gray>Max Level: <white>" + enchant.getMaxLevel());
        loreLines.add("<gray>Trigger: <white>" + enchant.getTrigger().name());
        loreLines.add("<gray>Chance: <white>" + enchant.getBaseChance() + "%");
        loreLines.add("");
        loreLines.add("<yellow>Click to get a book");

        Material mat = switch (enchant.getRarity()) {
            case COMMON    -> Material.BOOK;
            case UNCOMMON  -> Material.WRITABLE_BOOK;
            case RARE      -> Material.WRITTEN_BOOK;
            case EPIC      -> Material.ENCHANTED_BOOK;
            case LEGENDARY -> Material.ENCHANTED_BOOK;
            case MYTHIC    -> Material.ENCHANTED_BOOK;
            case SPECIAL   -> Material.ENCHANTED_BOOK;
        };

        var item = new ItemBuilder(mat)
                .name(enchant.getDisplayName())
                .lore(loreLines, true)
                .glow(enchant.getRarity().ordinal() >= 3)
                .build();

        return new GUIItem(item, click -> {
            ItemStack book = plugin.getEnchantManager().createEnchantBook(enchant, 1);
            player.closeInventory();
            player.getInventory().addItem(book);
            player.sendMessage(ColorUtils.parse("<green>Given <yellow>" +
                    enchant.getDisplayName() + " I</yellow> book."));
        });
    }

    @Override
    public void handleClick(int slot, ClickType click) {
        if (slot == 45) { // close
            player.closeInventory();
            return;
        }
        if (slot == PREV_SLOT && page > 1) {
            page--;
            refresh();
            return;
        }
        if (slot == NEXT_SLOT) {
            page++;
            refresh();
            return;
        }

        // Enchant slot click
        int perPage = plugin.getConfigManager().getMainConfig()
                .getInt("gui.enchants-per-page", 45);
        int startIndex = (page - 1) * perPage;
        if (slot < perPage && (startIndex + slot) < enchantList.size()) {
            CustomEnchant enchant = enchantList.get(startIndex + slot);
            var guiItem = buildEnchantItem(enchant);
            guiItem.click(click);
        }
    }
}
