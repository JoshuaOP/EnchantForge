package com.joshuaop.enchantforge.enchants;

public enum EnchantRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY,
    MYTHIC,
    SPECIAL;

    public static EnchantRarity fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }
}
