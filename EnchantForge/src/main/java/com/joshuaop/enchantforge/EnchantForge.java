package com.joshuaop.enchantforge;

import com.joshuaop.enchantforge.commands.EnchantForgeCommand;
import com.joshuaop.enchantforge.database.DatabaseManager;
import com.joshuaop.enchantforge.hooks.FloodgateHook;
import com.joshuaop.enchantforge.hooks.PlaceholderAPIHook;
import com.joshuaop.enchantforge.hooks.VaultHook;
import com.joshuaop.enchantforge.listeners.AnvilListener;
import com.joshuaop.enchantforge.listeners.EnchantListener;
import com.joshuaop.enchantforge.listeners.GUIListener;
import com.joshuaop.enchantforge.listeners.PlayerListener;
import com.joshuaop.enchantforge.managers.AnvilManager;
import com.joshuaop.enchantforge.managers.ConfigManager;
import com.joshuaop.enchantforge.managers.EffectManager;
import com.joshuaop.enchantforge.managers.EnchantManager;
import com.joshuaop.enchantforge.managers.GUIManager;
import com.joshuaop.enchantforge.managers.ScrollManager;
import com.joshuaop.enchantforge.utils.ColorUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class EnchantForge extends JavaPlugin {

    private static EnchantForge instance;

    private ConfigManager configManager;
    private EnchantManager enchantManager;
    private GUIManager guiManager;
    private EffectManager effectManager;
    private DatabaseManager databaseManager;
    private ScrollManager scrollManager;
    private AnvilManager anvilManager;

    private FloodgateHook floodgateHook;
    private VaultHook vaultHook;
    private PlaceholderAPIHook placeholderAPIHook;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("╔══════════════════════════════════╗");
        getLogger().info("║      EnchantForge v" + getDescription().getVersion() + "          ║");
        getLogger().info("║   Premium Custom Enchantments    ║");
        getLogger().info("╚══════════════════════════════════╝");

        // Step 1: Load configuration
        configManager = new ConfigManager(this);
        configManager.loadAll();

        // Step 2: Initialize hooks
        loadHooks();

        // Step 3: Initialize managers
        effectManager = new EffectManager(this);
        databaseManager = new DatabaseManager(this);
        enchantManager = new EnchantManager(this);
        scrollManager = new ScrollManager(this);
        anvilManager = new AnvilManager(this);
        guiManager = new GUIManager(this);

        // Step 4: Connect database async
        getServer().getAsyncScheduler().runNow(this, task -> {
            try {
                databaseManager.connect();
                databaseManager.createTables();
                getLogger().info("Database connected successfully.");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to connect to database!", e);
            }
        });

        // Step 5: Load enchants (built-in + YAML custom)
        enchantManager.loadAll();

        // Step 6: Register listeners
        registerListeners();

        // Step 7: Register commands
        var command = getServer().getPluginCommand("ce");
        if (command != null) {
            var handler = new EnchantForgeCommand(this);
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }

        // Step 8: Schedule async periodic save
        long saveIntervalTicks = configManager.getMainConfig().getLong("database.save-interval", 200L);
        long saveIntervalMs = saveIntervalTicks * 50L;
        getServer().getAsyncScheduler().runAtFixedRate(
                this,
                task -> databaseManager.saveAll(),
                saveIntervalMs,
                saveIntervalMs,
                TimeUnit.MILLISECONDS
        );

        getLogger().info("EnchantForge enabled — " + enchantManager.getEnchantCount() + " enchants loaded.");
    }

    @Override
    public void onDisable() {
        if (guiManager != null) guiManager.closeAll();
        if (databaseManager != null) {
            databaseManager.saveAll();
            databaseManager.disconnect();
        }
        getLogger().info("EnchantForge disabled. Goodbye!");
    }

    private void loadHooks() {
        floodgateHook = new FloodgateHook(this);
        floodgateHook.load();

        vaultHook = new VaultHook(this);
        vaultHook.load();

        placeholderAPIHook = new PlaceholderAPIHook(this);
        placeholderAPIHook.load();
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new EnchantListener(this), this);
        pm.registerEvents(new AnvilListener(this), this);
        pm.registerEvents(new GUIListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
    }

    public void reload() {
        configManager.loadAll();
        enchantManager.loadAll();
        scrollManager.reload();
        getLogger().info("EnchantForge reloaded successfully.");
    }

    // ── Static accessors ──────────────────────────────────────

    public static EnchantForge getInstance() { return instance; }
    public static MiniMessage mm() { return ColorUtils.MINI_MESSAGE; }

    // ── Manager accessors ─────────────────────────────────────

    public ConfigManager getConfigManager() { return configManager; }
    public EnchantManager getEnchantManager() { return enchantManager; }
    public GUIManager getGUIManager() { return guiManager; }
    public EffectManager getEffectManager() { return effectManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public ScrollManager getScrollManager() { return scrollManager; }
    public AnvilManager getAnvilManager() { return anvilManager; }

    // ── Hook accessors ────────────────────────────────────────

    public FloodgateHook getFloodgateHook() { return floodgateHook; }
    public VaultHook getVaultHook() { return vaultHook; }
    public PlaceholderAPIHook getPlaceholderAPIHook() { return placeholderAPIHook; }
}
