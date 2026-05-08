package com.joshuaop.enchantforge.database;

import java.util.Map;
import java.util.UUID;

/**
 * Database abstraction interface for EnchantForge.
 * Implementations: SQLiteDatabase, MySQLDatabase.
 */
public interface Database {

    void connect() throws Exception;
    void disconnect();
    void createTables();

    /** Load all custom enchant data for a player. Returns map of itemSlot -> enchantData string. */
    Map<String, String> loadPlayerData(UUID uuid);

    /** Save all custom enchant data for a player. */
    void savePlayerData(UUID uuid, Map<String, String> data);

    /** Delete all stored data for a player. */
    void deletePlayerData(UUID uuid);

    /** Check if the database connection is alive. */
    boolean isConnected();
}
