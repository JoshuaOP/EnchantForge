package com.joshuaop.enchantforge.managers;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.gui.GUI;
import com.joshuaop.enchantforge.gui.impl.CategoryMenuGUI;
import com.joshuaop.enchantforge.gui.impl.EnchantBrowserGUI;
import com.joshuaop.enchantforge.gui.impl.ScrollEditorGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUIManager {

    private final EnchantForge plugin;
    private final Map<UUID, GUI> openGuis = new ConcurrentHashMap<>();

    public GUIManager(EnchantForge plugin) {
        this.plugin = plugin;
    }

    public void openEnchantBrowser(Player player, int page) {
        boolean bedrock = plugin.getFloodgateHook().isBedrockPlayer(player);
        EnchantBrowserGUI gui = new EnchantBrowserGUI(plugin, player, page, bedrock);
        open(player, gui);
    }

    public void openCategoryMenu(Player player) {
        boolean bedrock = plugin.getFloodgateHook().isBedrockPlayer(player);
        CategoryMenuGUI gui = new CategoryMenuGUI(plugin, player, bedrock);
        open(player, gui);
    }

    public void openScrollEditor(Player player) {
        boolean bedrock = plugin.getFloodgateHook().isBedrockPlayer(player);
        ScrollEditorGUI gui = new ScrollEditorGUI(plugin, player, bedrock);
        open(player, gui);
    }

    private void open(Player player, GUI gui) {
        closeExisting(player);
        openGuis.put(player.getUniqueId(), gui);
        gui.open();
    }

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        GUI gui = openGuis.get(player.getUniqueId());
        if (gui == null) return;
        if (!event.getInventory().equals(gui.getInventory())) return;
        event.setCancelled(true);
        gui.handleClick(event.getSlot(), event.getClick());
    }

    public void handleClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        openGuis.remove(player.getUniqueId());
    }

    public boolean isGUIOpen(Player player) {
        return openGuis.containsKey(player.getUniqueId());
    }

    public GUI getOpenGUI(Player player) {
        return openGuis.get(player.getUniqueId());
    }

    private void closeExisting(Player player) {
        GUI existing = openGuis.remove(player.getUniqueId());
        if (existing != null) player.closeInventory();
    }

    public void closeAll() {
        for (UUID uuid : openGuis.keySet()) {
            var player = plugin.getServer().getPlayer(uuid);
            if (player != null) player.closeInventory();
        }
        openGuis.clear();
    }
}
