package com.joshuaop.enchantforge.database;

import com.joshuaop.enchantforge.EnchantForge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {

    private final EnchantForge plugin;
    private Database database;

    /** In-memory cache: uuid -> (slotKey -> enchantData) */
    private final Map<UUID, Map<String, String>> cache = new ConcurrentHashMap<>();
    /** Dirty flag — UUIDs queued for saving */
    private final Map<UUID, Boolean> dirty = new ConcurrentHashMap<>();

    public DatabaseManager(EnchantForge plugin) {
        this.plugin = plugin;
    }

    public void connect() throws Exception {
        String type = plugin.getConfigManager().getMainConfig()
                .getString("database.type", "SQLITE").toUpperCase();
        database = switch (type) {
            case "MYSQL" -> new MySQLDatabase(plugin);
            default      -> new SQLiteDatabase(plugin);
        };
        database.connect();
    }

    public void createTables() {
        if (database != null) database.createTables();
    }

    public void disconnect() {
        saveAll();
        if (database != null) database.disconnect();
    }

    // ── Cache-first read ───────────────────────────────────────

    public Map<String, String> getPlayerData(UUID uuid) {
        return cache.computeIfAbsent(uuid, u -> {
            if (database != null && database.isConnected()) {
                return new HashMap<>(database.loadPlayerData(u));
            }
            return new HashMap<>();
        });
    }

    public void setPlayerData(UUID uuid, String slotKey, String enchantData) {
        cache.computeIfAbsent(uuid, u -> new HashMap<>())
             .put(slotKey, enchantData);
        dirty.put(uuid, true);
    }

    public void invalidateCache(UUID uuid) {
        cache.remove(uuid);
    }

    // ── Periodic flush ────────────────────────────────────────

    public void saveAll() {
        if (database == null || !database.isConnected()) return;
        for (UUID uuid : dirty.keySet()) {
            Map<String, String> data = cache.get(uuid);
            if (data != null) {
                database.savePlayerData(uuid, data);
            }
        }
        dirty.clear();
    }

    public void savePlayer(UUID uuid) {
        if (database == null || !database.isConnected()) return;
        Map<String, String> data = cache.get(uuid);
        if (data != null) {
            database.savePlayerData(uuid, data);
            dirty.remove(uuid);
        }
    }

    public boolean isConnected() {
        return database != null && database.isConnected();
    }
}
