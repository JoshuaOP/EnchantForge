package com.joshuaop.enchantforge.enchants.impl;

import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantCategory;
import com.joshuaop.enchantforge.enchants.EnchantRarity;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

public class RocketBoostEnchant extends CustomEnchant {

    public RocketBoostEnchant() {
        super(
                "rocketboost",
                "<aqua>RocketBoost",
                "Launches you skyward when you jump.",
                EnchantRarity.EPIC,
                5,
                EnchantCategory.BOOTS.getMaterials(),
                EnchantTrigger.JUMP,
                15.0,
                5.0,
                3L
        );
    }

    @Override
    public void apply(Player player, int level, Event event) {
        double boost = 0.5 + (level * 0.3);
        Vector velocity = player.getVelocity().add(new Vector(0, boost, 0));
        player.setVelocity(velocity);

        player.getWorld().spawnParticle(
                org.bukkit.Particle.CLOUD,
                player.getLocation(),
                10, 0.3, 0, 0.3, 0.05
        );
        player.getWorld().playSound(
                player.getLocation(),
                org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
                0.8f, 1.2f
        );
    }
}
