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

public class ExecuteEnchant extends CustomEnchant {

    public ExecuteEnchant() {
        super(
                "execute",
                "<dark_red>Execute",
                "Deals bonus damage to low-health targets.",
                EnchantRarity.LEGENDARY,
                5,
                EnchantCategory.SWORD.getMaterials(),
                EnchantTrigger.ATTACK,
                100.0,
                0.0,
                0L
        );
    }

    @Override
    public void apply(Player player, int level, Event event) {
        if (!(event instanceof EntityDamageByEntityEvent dmgEvent)) return;
        Entity target = dmgEvent.getEntity();
        if (!(target instanceof LivingEntity living)) return;

        double healthPercent = (living.getHealth() / living.getAttribute(
                org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()) * 100.0;

        // Trigger only when target is below (40 - level*5)% health
        double threshold = 40.0 - (level * 5.0);
        if (healthPercent > threshold) return;

        double bonusDamage = dmgEvent.getDamage() * (level * 0.25);
        dmgEvent.setDamage(dmgEvent.getDamage() + bonusDamage);

        player.getWorld().spawnParticle(
                org.bukkit.Particle.CRIT,
                living.getLocation().add(0, 1, 0),
                20, 0.4, 0.4, 0.4, 0.1
        );
    }
}
