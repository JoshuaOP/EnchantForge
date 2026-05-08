package com.joshuaop.enchantforge.gui;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Wraps an ItemStack with an optional click action.
 */
public class GUIItem {

    private final ItemStack item;
    private final Consumer<ClickType> action;

    public GUIItem(ItemStack item) {
        this(item, null);
    }

    public GUIItem(ItemStack item, Consumer<ClickType> action) {
        this.item = item;
        this.action = action;
    }

    public ItemStack getItem() { return item; }

    public void click(ClickType type) {
        if (action != null) action.accept(type);
    }

    public boolean hasAction() { return action != null; }
}
