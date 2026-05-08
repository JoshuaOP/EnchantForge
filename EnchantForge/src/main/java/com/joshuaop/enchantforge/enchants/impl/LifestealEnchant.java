package com.joshuaop.enchantforge.enchants.impl;

import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantCategory;
import com.joshuaop.enchantforge.enchants.EnchantRarity;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LifestealEnchant extends CustomEnchant {

    public LifestealEnchant() {
        super(
                "lifesteal",
                "<red>Lifesteal",
                "Heals you on hit.",
                EnchantRarity.EPIC,
                5,
                EnchantCategory.SWORD.getMaterials(),
                EnchantTrigger.ATTACK,
                20.0,
                5.0,
                0L
        );
    }

    @Override
    public void apply(Player player, int level, Event event) {
        double healAmount = level * 0.5; // 0.5 hearts per level
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;
        double newHealth = Math.min(player.getHealth() + healAmount, maxHealth.getValue());
        player.setHealth(newHealth);
        player.getWorld().spawnParticle(
                org.bukkit.Particle.HEART,
                player.getLocation().add(0, 1, 0),
                level, 0.3, 0.3, 0.3, 0
        );
    }
}
