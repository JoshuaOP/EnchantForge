package com.joshuaop.enchantforge.listeners;

import com.joshuaop.enchantforge.EnchantForge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GUIListener implements Listener {

    private final EnchantForge plugin;

    public GUIListener(EnchantForge plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        plugin.getGUIManager().handleClick(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        // Block drags inside any open GUI
        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player)) return;
        if (plugin.getGUIManager().isGUIOpen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        plugin.getGUIManager().handleClose(event);
    }
}
