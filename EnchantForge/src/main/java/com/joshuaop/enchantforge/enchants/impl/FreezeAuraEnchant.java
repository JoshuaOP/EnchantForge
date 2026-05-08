package com.joshuaop.enchantforge.enchants.impl;

import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantCategory;
import com.joshuaop.enchantforge.enchants.EnchantRarity;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class FreezeAuraEnchant extends CustomEnchant {

    public FreezeAuraEnchant() {
        super(
                "freezeaura",
                "<aqua>FreezeAura",
                "Slows and freezes nearby enemies on hit.",
                EnchantRarity.RARE,
                5,
                EnchantCategory.CHESTPLATE.getMaterials(),
                EnchantTrigger.DAMAGE,
                25.0,
                5.0,
                5L
        );
    }

    @Override
    public void apply(Player player, int level, Event event) {
        double radius = 3.0 + level;
        int duration = 40 + (level * 20); // ticks
        int amplifier = Math.min(level - 1, 3);

        for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
            if (!(nearby instanceof LivingEntity living)) continue;
            if (nearby.equals(player)) continue;

            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, amplifier));
            living.setFreezeTicks(duration);
            player.getWorld().spawnParticle(
                    org.bukkit.Particle.SNOWFLAKE,
                    living.getLocation().add(0, 1, 0),
                    15, 0.3, 0.5, 0.3, 0.01
            );
        }
    }
}
