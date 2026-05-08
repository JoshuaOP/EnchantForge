package com.joshuaop.enchantforge.gui;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.utils.ColorUtils;
import com.joshuaop.enchantforge.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

/**
 * Abstract base for all EnchantForge GUI menus.
 * Handles inventory creation, filler placement, and the click dispatch contract.
 */
public abstract class GUI {

    protected final EnchantForge plugin;
    protected final Player player;
    protected final boolean bedrockPlayer;
    protected Inventory inventory;

    protected GUI(EnchantForge plugin, Player player, boolean bedrockPlayer) {
        this.plugin = plugin;
        this.player = player;
        this.bedrockPlayer = bedrockPlayer;
    }

    // ── Abstract contract ──────────────────────────────────────

    /** Build and populate the inventory. Called by open(). */
    protected abstract void build();

    /** Handle a slot click. */
    public abstract void handleClick(int slot, ClickType click);

    // ── Lifecycle ──────────────────────────────────────────────

    /** Open the GUI for the player. */
    public void open() {
        build();
        player.openInventory(inventory);
    }

    /** Refresh the GUI in-place without closing. */
    public void refresh() {
        if (inventory == null) return;
        inventory.clear();
        build();
    }

    // ── Helpers ────────────────────────────────────────────────

    protected Inventory createInventory(Component title, int rows) {
        int size = Math.min(Math.max(rows, 1), 6) * 9;
        inventory = Bukkit.createInventory(null, size, title);
        return inventory;
    }

    protected void fillBorder(int rows) {
        var filler = ItemBuilder.filler();
        int size = rows * 9;
        for (int i = 0; i < 9; i++) inventory.setItem(i, filler);
        for (int i = size - 9; i < size; i++) inventory.setItem(i, filler);
        for (int i = 0; i < rows; i++) {
            inventory.setItem(i * 9, filler);
            inventory.setItem(i * 9 + 8, filler);
        }
    }

    protected void fill(int rows) {
        var filler = ItemBuilder.filler();
        for (int i = 0; i < rows * 9; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, filler);
        }
    }

    protected void setItem(int slot, GUIItem guiItem) {
        if (slot < 0 || slot >= inventory.getSize()) return;
        inventory.setItem(slot, guiItem.getItem());
    }

    public Inventory getInventory() { return inventory; }
    public Player getPlayer() { return player; }
    public boolean isBedrockPlayer() { return bedrockPlayer; }
}
