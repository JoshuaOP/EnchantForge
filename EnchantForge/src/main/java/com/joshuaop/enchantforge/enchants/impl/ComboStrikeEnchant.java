package com.joshuaop.enchantforge.enchants.impl;

import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantCategory;
import com.joshuaop.enchantforge.enchants.EnchantRarity;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ComboStrikeEnchant extends CustomEnchant {

    private final Map<UUID, Integer> comboCounts = new HashMap<>();
    private final Map<UUID, Long> lastHitTime = new HashMap<>();

    private static final long COMBO_WINDOW_MS = 2000L; // 2 seconds

    public ComboStrikeEnchant() {
        super(
                "combostrike",
                "<light_purple>ComboStrike",
                "Each consecutive hit deals more damage.",
                EnchantRarity.RARE,
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

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long lastHit = lastHitTime.getOrDefault(uuid, 0L);

        int combo;
        if (now - lastHit > COMBO_WINDOW_MS) {
            combo = 1;
        } else {
            combo = Math.min(comboCounts.getOrDefault(uuid, 0) + 1, 5 + level);
        }

        comboCounts.put(uuid, combo);
        lastHitTime.put(uuid, now);

        if (combo > 1) {
            double bonus = (combo - 1) * (level * 0.15);
            dmgEvent.setDamage(dmgEvent.getDamage() * (1.0 + bonus));
            player.getWorld().spawnParticle(
                    org.bukkit.Particle.SWEEP_ATTACK,
                    player.getLocation().add(0, 1, 0),
                    1, 0, 0, 0, 0
            );
        }
    }
}
