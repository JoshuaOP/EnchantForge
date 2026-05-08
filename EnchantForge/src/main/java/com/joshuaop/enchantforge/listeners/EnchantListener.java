package com.joshuaop.enchantforge.listeners;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import com.joshuaop.enchantforge.utils.EnchantUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnchantListener implements Listener {

    private final EnchantForge plugin;
    /** Per-player cooldown tracking: enchantId -> last trigger time (ms) */
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public EnchantListener(EnchantForge plugin) {
        this.plugin = plugin;
    }

    // ── ATTACK ────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack held = player.getInventory().getItemInMainHand();
        triggerEnchants(player, held, EnchantTrigger.ATTACK, event);
    }

    // ── DAMAGE (taken) ────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        // Check all equipped armor pieces
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) continue;
            triggerEnchants(player, armor, EnchantTrigger.DAMAGE, event);
        }
    }

    // ── BLOCK_BREAK ───────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        triggerEnchants(player, tool, EnchantTrigger.BLOCK_BREAK, event);
    }

    // ── SHOOT ─────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        triggerEnchants(player, event.getBow(), EnchantTrigger.SHOOT, event);
    }

    // ── KILL ──────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        ItemStack held = killer.getInventory().getItemInMainHand();
        triggerEnchants(killer, held, EnchantTrigger.KILL, event);
    }

    // ── FISH ──────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        triggerEnchants(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand(),
                EnchantTrigger.FISH, event);
    }

    // ── MOVE / JUMP ───────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;
        Player player = event.getPlayer();
        // MOVE trigger — boots
        ItemStack boots = player.getInventory().getBoots();
        if (boots != null) triggerEnchants(player, boots, EnchantTrigger.MOVE, event);
    }

    // ── Core dispatch ──────────────────────────────────────────

    private void triggerEnchants(Player player, ItemStack item,
                                  EnchantTrigger trigger, org.bukkit.event.Event event) {
        if (item == null) return;
        Map<String, Integer> enchants = EnchantUtils.getEnchants(item);
        if (enchants.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            CustomEnchant ce = plugin.getEnchantManager().getEnchant(entry.getKey());
            if (ce == null || !ce.isEnabled()) continue;
            if (ce.getTrigger() != trigger) continue;
            if (!ce.canApply(item)) continue;

            // Cooldown check
            if (isOnCooldown(player, ce)) continue;

            // Chance roll
            if (!ce.rollChance(entry.getValue())) continue;

            ce.apply(player, entry.getValue(), event);
            applyBypassCooldown(player, ce);
        }
    }

    private boolean isOnCooldown(Player player, CustomEnchant ce) {
        if (ce.getCooldownSeconds() <= 0) return false;
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(
                player.getUniqueId(), u -> new HashMap<>());
        Long lastTrigger = playerCooldowns.get(ce.getId());
        if (lastTrigger == null) return false;
        return System.currentTimeMillis() - lastTrigger < ce.getCooldownSeconds() * 1000L;
    }

    private void applyBypassCooldown(Player player, CustomEnchant ce) {
        if (ce.getCooldownSeconds() <= 0) return;
        cooldowns.computeIfAbsent(player.getUniqueId(), u -> new HashMap<>())
                 .put(ce.getId(), System.currentTimeMillis());
    }
}
