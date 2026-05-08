package com.joshuaop.enchantforge.managers;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.hooks.VaultHook;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.logging.Level;

/**
 * Parses and applies YAML-defined effect strings.
 *
 * Supported formats:
 *   LIGHTNING
 *   EXPLOSION
 *   FIRE
 *   FREEZE
 *   POTION:<type>:<duration>:<amplifier>
 *   PARTICLE:<type>
 *   SOUND:<sound>
 *   HEAL:<amount>
 *   TELEPORT
 *   VELOCITY:<x>:<y>:<z>
 *   XP:<amount>
 *   ECONOMY:<amount>
 */
public class EffectManager {

    private final EnchantForge plugin;

    public EffectManager(EnchantForge plugin) {
        this.plugin = plugin;
    }

    public void applyEffect(Player player, String effectDef, int level, Event event) {
        if (effectDef == null || effectDef.isBlank()) return;
        String upper = effectDef.trim().toUpperCase();
        String[] parts = upper.split(":");

        try {
            switch (parts[0]) {
                case "LIGHTNING" -> applyLightning(player, event);
                case "EXPLOSION" -> applyExplosion(player, event);
                case "FIRE"      -> applyFire(player, event, level);
                case "FREEZE"    -> applyFreeze(player, event, level);
                case "POTION"    -> applyPotion(player, event, parts);
                case "PARTICLE"  -> applyParticle(player, event, parts);
                case "SOUND"     -> applySound(player, parts);
                case "HEAL"      -> applyHeal(player, parts, level);
                case "TELEPORT"  -> applyTeleport(player, event);
                case "VELOCITY"  -> applyVelocity(player, parts);
                case "XP"        -> applyXp(player, parts, level);
                case "ECONOMY"   -> applyEconomy(player, parts, level);
                default -> plugin.getLogger().warning("Unknown effect type: " + parts[0]);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error applying effect '" + effectDef + "': " + e.getMessage());
        }
    }

    private LivingEntity getTarget(Player player, Event event) {
        if (event instanceof EntityDamageByEntityEvent dmg &&
                dmg.getEntity() instanceof LivingEntity le) {
            return le;
        }
        return null;
    }

    private Location getTargetLocation(Player player, Event event) {
        LivingEntity target = getTarget(player, event);
        return target != null ? target.getLocation() : player.getLocation();
    }

    private void applyLightning(Player player, Event event) {
        Location loc = getTargetLocation(player, event);
        loc.getWorld().strikeLightningEffect(loc);
    }

    private void applyExplosion(Player player, Event event) {
        Location loc = getTargetLocation(player, event);
        loc.getWorld().createExplosion(loc, 0F, false, false, player);
    }

    private void applyFire(Player player, Event event, int level) {
        LivingEntity target = getTarget(player, event);
        if (target != null) target.setFireTicks(20 * (level + 2));
    }

    private void applyFreeze(Player player, Event event, int level) {
        LivingEntity target = getTarget(player, event);
        if (target != null) {
            target.setFreezeTicks(20 * (level + 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * level, 2));
        }
    }

    private void applyPotion(Player player, Event event, String[] parts) {
        // POTION:<type>:<duration>:<amplifier>
        PotionEffectType type;
        try {
            type = PotionEffectType.getByName(parts.length > 1 ? parts[1] : "");
        } catch (Exception e) {
            return;
        }
        if (type == null) return;
        int duration = parts.length > 2 ? Integer.parseInt(parts[2]) : 60;
        int amplifier = parts.length > 3 ? Integer.parseInt(parts[3]) : 0;

        LivingEntity target = getTarget(player, event);
        if (target != null) target.addPotionEffect(new PotionEffect(type, duration, amplifier));
    }

    private void applyParticle(Player player, Event event, String[] parts) {
        if (parts.length < 2) return;
        Particle particle;
        try {
            particle = Particle.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            return;
        }
        Location loc = getTargetLocation(player, event).add(0, 1, 0);
        loc.getWorld().spawnParticle(particle, loc, 15, 0.3, 0.3, 0.3, 0.05);
    }

    private void applySound(Player player, String[] parts) {
        if (parts.length < 2) return;
        Sound sound;
        try {
            sound = Sound.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            return;
        }
        player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    private void applyHeal(Player player, String[] parts, int level) {
        double amount = parts.length > 1 ? Double.parseDouble(parts[1]) : 2.0;
        amount *= level;
        AttributeInstance maxHp = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHp == null) return;
        player.setHealth(Math.min(player.getHealth() + amount, maxHp.getValue()));
    }

    private void applyTeleport(Player player, Event event) {
        LivingEntity target = getTarget(player, event);
        if (target != null) {
            // Teleport target behind the player
            Location behind = player.getLocation().add(
                    player.getLocation().getDirection().normalize().multiply(-3));
            target.teleport(behind);
        }
    }

    private void applyVelocity(Player player, String[] parts) {
        double vx = parts.length > 1 ? Double.parseDouble(parts[1]) : 0;
        double vy = parts.length > 2 ? Double.parseDouble(parts[2]) : 0.5;
        double vz = parts.length > 3 ? Double.parseDouble(parts[3]) : 0;
        player.setVelocity(player.getVelocity().add(new Vector(vx, vy, vz)));
    }

    private void applyXp(Player player, String[] parts, int level) {
        int amount = (parts.length > 1 ? Integer.parseInt(parts[1]) : 5) * level;
        player.giveExp(amount);
    }

    private void applyEconomy(Player player, String[] parts, int level) {
        VaultHook vault = plugin.getVaultHook();
        if (!vault.isEnabled()) return;
        double amount = (parts.length > 1 ? Double.parseDouble(parts[1]) : 10.0) * level;
        vault.deposit(player, amount);
    }
}
