package com.joshuaop.enchantforge.enchants;

import com.joshuaop.enchantforge.utils.ColorUtils;
import com.joshuaop.enchantforge.utils.EnchantUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

/**
 * Base class for all EnchantForge custom enchantments.
 * Extend this to create a new built-in enchant.
 */
public abstract class CustomEnchant {

    private final String id;
    private final String displayName;
    private final String description;
    private final EnchantRarity rarity;
    private final int maxLevel;
    private final Set<Material> applicableMaterials;
    private final EnchantTrigger trigger;
    private final double baseChance;
    private final double chancePerLevel;
    private final long cooldownSeconds;

    private boolean enabled = true;

    protected CustomEnchant(
            String id,
            String displayName,
            String description,
            EnchantRarity rarity,
            int maxLevel,
            Set<Material> applicableMaterials,
            EnchantTrigger trigger,
            double baseChance,
            double chancePerLevel,
            long cooldownSeconds
    ) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
        this.maxLevel = maxLevel;
        this.applicableMaterials = applicableMaterials;
        this.trigger = trigger;
        this.baseChance = baseChance;
        this.chancePerLevel = chancePerLevel;
        this.cooldownSeconds = cooldownSeconds;
    }

    // ── Abstract contract ──────────────────────────────────────

    /**
     * Execute this enchant's effect.
     *
     * @param player the player who triggered the enchant
     * @param level  the current enchant level on the item
     * @param event  the triggering Bukkit event (may be cast as needed)
     */
    public abstract void apply(Player player, int level, Event event);

    // ── Trigger helpers ────────────────────────────────────────

    public boolean canApply(ItemStack item) {
        if (item == null) return false;
        if (applicableMaterials.isEmpty()) return true; // UNIVERSAL
        return applicableMaterials.contains(item.getType());
    }

    public boolean rollChance(int level) {
        double chance = baseChance + chancePerLevel * (level - 1);
        return EnchantUtils.chance(chance);
    }

    // ── Lore building ──────────────────────────────────────────

    public Component buildLoreLine(int level) {
        String rarityColor = "<gray>";
        String line = rarityColor + displayName + " " + ColorUtils.toRoman(level);
        return ColorUtils.parse(line);
    }

    public List<Component> buildDescriptionLines() {
        return List.of(ColorUtils.parse("<dark_gray>" + description));
    }

    // ── Getters ────────────────────────────────────────────────

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public EnchantRarity getRarity() { return rarity; }
    public int getMaxLevel() { return maxLevel; }
    public Set<Material> getApplicableMaterials() { return applicableMaterials; }
    public EnchantTrigger getTrigger() { return trigger; }
    public double getBaseChance() { return baseChance; }
    public double getChancePerLevel() { return chancePerLevel; }
    public long getCooldownSeconds() { return cooldownSeconds; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
