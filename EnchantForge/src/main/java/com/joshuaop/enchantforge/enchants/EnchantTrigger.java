package com.joshuaop.enchantforge.enchants;

public enum EnchantTrigger {
    ATTACK,
    BLOCK_BREAK,
    DAMAGE,
    SHOOT,
    MOVE,
    JUMP,
    FISH,
    KILL,
    ARMOR_EQUIP,
    PASSIVE;

    public static EnchantTrigger fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PASSIVE;
        }
    }
}
