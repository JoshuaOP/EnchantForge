package com.joshuaop.enchantforge.gui.impl;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.gui.GUI;
import com.joshuaop.enchantforge.utils.ColorUtils;
import com.joshuaop.enchantforge.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ScrollEditorGUI extends GUI {

    private static final int ROWS = 3;

    // Display slots for each scroll type
    private static final List<String> SCROLL_IDS = List.of(
            "white_scroll", "black_scroll", "magic_dust", "upgrade_scroll", "transmog_scroll"
    );

    public ScrollEditorGUI(EnchantForge plugin, Player player, boolean bedrock) {
        super(plugin, player, bedrock);
    }

    @Override
    protected void build() {
        String title = plugin.getConfigManager().getMessagesConfig()
                .getString("gui.scroll-editor-title", "<dark_gray>✦ Scroll Editor ✦");
        createInventory(ColorUtils.parse(title), ROWS);
        fillBorder(ROWS);

        for (int i = 0; i < SCROLL_IDS.size(); i++) {
            String scrollId = SCROLL_IDS.get(i);
            ItemStack scroll = plugin.getScrollManager().createScroll(scrollId);
            if (scroll != null) {
                int slot = 10 + i;
                inventory.setItem(slot, scroll);
            }
        }

        // Get button
        inventory.setItem(22, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .name("<green>Click a scroll above, then click here to get it")
                .build());

        // Close
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

        for (int i = 0; i < SCROLL_IDS.size(); i++) {
            if (slot == 10 + i) {
                String scrollId = SCROLL_IDS.get(i);
                ItemStack scroll = plugin.getScrollManager().createScroll(scrollId);
                if (scroll != null) {
                    player.getInventory().addItem(scroll);
                    player.closeInventory();
                    player.sendMessage(ColorUtils.parse(
                            "<green>You received a <yellow>" + scrollId.replace("_", " ") + "</yellow>."));
                }
                return;
            }
        }
    }
}
