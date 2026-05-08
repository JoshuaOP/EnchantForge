package com.joshuaop.enchantforge.enchants;

import org.bukkit.Material;

import java.util.Set;

public enum EnchantCategory {

    SWORD(Set.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD
    )),
    AXE(Set.of(
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE
    )),
    PICKAXE(Set.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE
    )),
    SHOVEL(Set.of(
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL
    )),
    HOE(Set.of(
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE,
            Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE
    )),
    HELMET(Set.of(
            Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET,
            Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
            Material.TURTLE_HELMET
    )),
    CHESTPLATE(Set.of(
            Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE,
            Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE
    )),
    LEGGINGS(Set.of(
            Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS,
            Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS
    )),
    BOOTS(Set.of(
            Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS,
            Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS
    )),
    BOW(Set.of(Material.BOW)),
    CROSSBOW(Set.of(Material.CROSSBOW)),
    FISHING_ROD(Set.of(Material.FISHING_ROD)),
    ELYTRA(Set.of(Material.ELYTRA)),
    TRIDENT(Set.of(Material.TRIDENT)),
    UNIVERSAL(Set.of()) // handled specially — applies to all
    ;

    private final Set<Material> materials;

    EnchantCategory(Set<Material> materials) {
        this.materials = materials;
    }

    public Set<Material> getMaterials() {
        return materials;
    }

    public boolean matches(Material material) {
        if (this == UNIVERSAL) return true;
        return materials.contains(material);
    }

    public static EnchantCategory fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNIVERSAL;
        }
    }

    /** Resolve a material name (e.g. "SWORD") to a category set. */
    public static Set<Material> resolveMaterials(String token) {
        // Try as a category first
        for (EnchantCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(token)) {
                if (cat == UNIVERSAL) return Set.of();
                return cat.getMaterials();
            }
        }
        // Try as a direct material
        try {
            Material m = Material.valueOf(token.toUpperCase());
            return Set.of(m);
        } catch (IllegalArgumentException e) {
            return Set.of();
        }
    }
}
