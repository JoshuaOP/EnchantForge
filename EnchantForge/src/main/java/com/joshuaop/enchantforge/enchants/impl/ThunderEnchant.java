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

public class ThunderEnchant extends CustomEnchant {

    public ThunderEnchant() {
        super(
                "thunder",
                "<yellow>Thunder",
                "Strikes lightning on your enemy.",
                EnchantRarity.EPIC,
                5,
                EnchantCategory.SWORD.getMaterials(),
                EnchantTrigger.ATTACK,
                10.0,
                3.0,
                3L
        );
    }

    @Override
    public void apply(Player player, int level, Event event) {
        if (!(event instanceof EntityDamageByEntityEvent dmgEvent)) return;
        Entity target = dmgEvent.getEntity();
        if (!(target instanceof LivingEntity)) return;

        // Cosmetic lightning (no fire, no extra damage from bolt itself)
        target.getWorld().strikeLightningEffect(target.getLocation());

        // Extra damage scaling with level
        double bonus = level * 1.5;
        dmgEvent.setDamage(dmgEvent.getDamage() + bonus);
    }
}
