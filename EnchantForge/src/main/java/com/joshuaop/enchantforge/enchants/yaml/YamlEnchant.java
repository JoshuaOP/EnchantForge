package com.joshuaop.enchantforge.enchants.yaml;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantRarity;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import com.joshuaop.enchantforge.managers.EffectManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;
import java.util.Set;

/**
 * A custom enchant created entirely from YAML configuration.
 * Server admins can define these in enchants.yml without any Java code.
 */
public class YamlEnchant extends CustomEnchant {

    private final List<String> effectDefinitions;

    public YamlEnchant(
            String id,
            String displayName,
            String description,
            EnchantRarity rarity,
            int maxLevel,
            Set<Material> applicableMaterials,
            EnchantTrigger trigger,
            double baseChance,
            double chancePerLevel,
            long cooldown,
            List<String> effectDefinitions
    ) {
        super(id, displayName, description, rarity, maxLevel,
              applicableMaterials, trigger, baseChance, chancePerLevel, cooldown);
        this.effectDefinitions = effectDefinitions;
    }

    @Override
    public void apply(Player player, int level, Event event) {
        EffectManager effectManager = EnchantForge.getInstance().getEffectManager();
        for (String effectDef : effectDefinitions) {
            effectManager.applyEffect(player, effectDef, level, event);
        }
    }

    public List<String> getEffectDefinitions() {
        return effectDefinitions;
    }
}
