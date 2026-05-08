package com.joshuaop.enchantforge.managers;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import com.joshuaop.enchantforge.enchants.impl.*;
import com.joshuaop.enchantforge.enchants.yaml.YamlEnchant;
import com.joshuaop.enchantforge.enchants.yaml.YamlEnchantLoader;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EnchantManager {

    private final EnchantForge plugin;
    private final Map<String, CustomEnchant> enchants = new ConcurrentHashMap<>();
    private final Set<Component> enchantLoreComponents = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public EnchantManager(EnchantForge plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        enchants.clear();

        // Register all built-in enchants
        registerBuiltIn();

        // Load YAML-defined custom enchants
        ConfigurationSection customSection = plugin.getConfigManager()
                .getEnchantsConfig()
                .getConfigurationSection("custom-enchants");
        if (customSection != null) {
            YamlEnchantLoader loader = new YamlEnchantLoader(plugin.getLogger());
            List<YamlEnchant> yamlEnchants = loader.load(customSection);
            for (YamlEnchant ye : yamlEnchants) {
                registerEnchant(ye);
            }
            plugin.getLogger().info("Loaded " + yamlEnchants.size() + " YAML custom enchants.");
        }

        plugin.getLogger().info("Total enchants registered: " + enchants.size());
    }

    private void registerBuiltIn() {
        // Sword
        registerEnchant(new LifestealEnchant());
        registerEnchant(new ThunderEnchant());
        registerEnchant(new ExecuteEnchant());
        registerEnchant(new ComboStrikeEnchant());

        // Armor / Boots
        registerEnchant(new FreezeAuraEnchant());
        registerEnchant(new RocketBoostEnchant());

        // Pickaxe
        registerEnchant(new VeinMinerEnchant());
        registerEnchant(new AutoSmeltEnchant());
        registerEnchant(new ExplosiveEnchant());
        registerEnchant(new TelepathyEnchant());
    }

    public void registerEnchant(CustomEnchant enchant) {
        enchants.put(enchant.getId().toLowerCase(), enchant);
    }

    public CustomEnchant getEnchant(String id) {
        if (id == null) return null;
        return enchants.get(id.toLowerCase());
    }

    public boolean hasEnchant(String id) {
        return enchants.containsKey(id.toLowerCase());
    }

    public Collection<CustomEnchant> getAllEnchants() {
        return Collections.unmodifiableCollection(enchants.values());
    }

    public List<CustomEnchant> getEnchantsForTrigger(EnchantTrigger trigger) {
        return enchants.values().stream()
                .filter(e -> e.isEnabled() && e.getTrigger() == trigger)
                .collect(Collectors.toList());
    }

    public int getEnchantCount() {
        return enchants.size();
    }

    /**
     * Check whether a Component line matches any registered enchant's display format
     * (used to strip old enchant lore before rebuilding).
     */
    public boolean isEnchantLoreLine(Component line) {
        // We track built lore components in a cache. Since MiniMessage produces
        // components that may not compare perfectly, we serialize and compare strings.
        String serialized = EnchantForge.mm().serialize(line);
        for (CustomEnchant enchant : enchants.values()) {
            if (serialized.contains(com.joshuaop.enchantforge.utils.ColorUtils.strip(enchant.getDisplayName()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create an enchant book ItemStack for the given enchant and level.
     */
    public ItemStack createEnchantBook(CustomEnchant enchant, int level) {
        var cfg = plugin.getConfigManager().getMainConfig();
        String matName = cfg.getString("books.material", "BOOK");
        boolean glow = cfg.getBoolean("books.glow", true);

        org.bukkit.Material material;
        try {
            material = org.bukkit.Material.valueOf(matName);
        } catch (IllegalArgumentException e) {
            material = org.bukkit.Material.BOOK;
        }

        List<String> loreLines = new ArrayList<>();
        loreLines.add("<dark_gray>" + enchant.getDescription());
        loreLines.add("");
        loreLines.add("<gray>Rarity: " + enchant.getRarity().name());
        loreLines.add("<gray>Max Level: " + enchant.getMaxLevel());
        loreLines.add("");
        loreLines.add("<yellow>► Right-click an item to apply");

        var item = new com.joshuaop.enchantforge.utils.ItemBuilder(material)
                .name(enchant.getDisplayName() + " " +
                        com.joshuaop.enchantforge.utils.ColorUtils.toRoman(level))
                .lore(loreLines, true)
                .glow(glow)
                .pdc(com.joshuaop.enchantforge.utils.EnchantUtils.ENCHANT_BOOK_KEY,
                        org.bukkit.persistence.PersistentDataType.STRING,
                        enchant.getId() + ":" + level)
                .build();
        return item;
    }
}
