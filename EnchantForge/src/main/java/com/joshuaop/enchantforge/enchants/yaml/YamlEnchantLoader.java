package com.joshuaop.enchantforge.enchants.yaml;

import com.joshuaop.enchantforge.enchants.EnchantCategory;
import com.joshuaop.enchantforge.enchants.EnchantRarity;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Parses the custom-enchants section of enchants.yml into YamlEnchant instances.
 */
public final class YamlEnchantLoader {

    private final Logger logger;

    public YamlEnchantLoader(Logger logger) {
        this.logger = logger;
    }

    public List<YamlEnchant> load(ConfigurationSection section) {
        List<YamlEnchant> result = new ArrayList<>();
        if (section == null) return result;

        for (String key : section.getKeys(false)) {
            ConfigurationSection enchantSection = section.getConfigurationSection(key);
            if (enchantSection == null) continue;

            try {
                result.add(parse(key, enchantSection));
            } catch (Exception e) {
                logger.warning("Failed to load YAML enchant '" + key + "': " + e.getMessage());
            }
        }

        return result;
    }

    private YamlEnchant parse(String id, ConfigurationSection s) {
        String display = s.getString("display", id);
        String description = String.join(" ", s.getStringList("description"));

        EnchantRarity rarity = EnchantRarity.fromString(s.getString("rarity", "COMMON"));
        int maxLevel = s.getInt("max-level", 5);

        // Resolve item types
        Set<Material> materials = new HashSet<>();
        for (String typeToken : s.getStringList("item-types")) {
            Set<Material> resolved = EnchantCategory.resolveMaterials(typeToken);
            if (resolved.isEmpty() && typeToken.equalsIgnoreCase("UNIVERSAL")) {
                materials.clear();
                break; // universal — empty set means all
            }
            materials.addAll(resolved);
        }

        EnchantTrigger trigger = EnchantTrigger.fromString(s.getString("trigger", "PASSIVE"));
        double chance = s.getDouble("chance", 100.0);
        double chancePerLevel = s.getDouble("chance-per-level", 0.0);
        long cooldown = s.getLong("cooldown", 0L);

        List<String> effects = s.getStringList("effects");

        return new YamlEnchant(id, display, description, rarity, maxLevel,
                materials, trigger, chance, chancePerLevel, cooldown, effects);
    }
}
