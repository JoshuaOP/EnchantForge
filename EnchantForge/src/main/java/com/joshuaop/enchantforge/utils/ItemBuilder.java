package com.joshuaop.enchantforge.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public final class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(Component name) {
        meta.displayName(name);
        return this;
    }

    public ItemBuilder name(String miniMessage) {
        meta.displayName(ColorUtils.parse(miniMessage));
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        meta.lore(lore);
        return this;
    }

    public ItemBuilder lore(String... lines) {
        meta.lore(Arrays.stream(lines).map(ColorUtils::parse).toList());
        return this;
    }

    public ItemBuilder lore(List<String> lines, boolean parse) {
        if (parse) {
            meta.lore(lines.stream().map(ColorUtils::parse).toList());
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder glow(boolean glow) {
        if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder hideAll() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public ItemBuilder unbreakable(boolean value) {
        meta.setUnbreakable(value);
        return this;
    }

    public ItemBuilder customModelData(int data) {
        meta.setCustomModelData(data);
        return this;
    }

    public <T, Z> ItemBuilder pdc(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        meta.getPersistentDataContainer().set(key, type, value);
        return this;
    }

    public ItemBuilder meta(Consumer<ItemMeta> consumer) {
        consumer.accept(meta);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    // ── Convenience factory methods ───────────────────────────

    public static ItemStack skull(String name, String lore) {
        return new ItemBuilder(Material.PLAYER_HEAD)
                .name(name)
                .lore(lore)
                .build();
    }

    public static ItemStack filler() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name("<gray> ")
                .hideAll()
                .build();
    }

    public static ItemStack barrier(String name) {
        return new ItemBuilder(Material.BARRIER)
                .name(name)
                .hideAll()
                .build();
    }
}
