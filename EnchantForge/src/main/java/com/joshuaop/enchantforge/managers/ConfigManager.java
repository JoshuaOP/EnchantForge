package com.joshuaop.enchantforge.managers;

import com.joshuaop.enchantforge.EnchantForge;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class ConfigManager {

    private final EnchantForge plugin;

    private FileConfiguration mainConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration enchantsConfig;
    private FileConfiguration scrollsConfig;
    private FileConfiguration raritiesConfig;

    public ConfigManager(EnchantForge plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        mainConfig = plugin.getConfig();

        messagesConfig = loadCustomConfig("messages.yml");
        enchantsConfig = loadCustomConfig("enchants.yml");
        scrollsConfig = loadCustomConfig("scrolls.yml");
        raritiesConfig = loadCustomConfig("rarities.yml");
    }

    private FileConfiguration loadCustomConfig(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Merge defaults from jar
        InputStream defStream = plugin.getResource(name);
        if (defStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            config.setDefaults(defaults);
        }
        return config;
    }

    public void saveCustomConfig(FileConfiguration config, String name) {
        File file = new File(plugin.getDataFolder(), name);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + name, e);
        }
    }

    public FileConfiguration getMainConfig()     { return mainConfig; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }
    public FileConfiguration getEnchantsConfig() { return enchantsConfig; }
    public FileConfiguration getScrollsConfig()  { return scrollsConfig; }
    public FileConfiguration getRaritiesConfig() { return raritiesConfig; }

    public String getMessage(String path) {
        String prefix = messagesConfig.getString("prefix", "");
        String msg = messagesConfig.getString(path, "<red>Missing message: " + path);
        return prefix + msg;
    }

    public String getMessageNoPrefix(String path) {
        return messagesConfig.getString(path, "<red>Missing message: " + path);
    }
}
