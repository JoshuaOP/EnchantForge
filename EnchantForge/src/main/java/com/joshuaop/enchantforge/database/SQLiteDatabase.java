package com.joshuaop.enchantforge.database;

import com.joshuaop.enchantforge.EnchantForge;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class SQLiteDatabase implements Database {

    private final EnchantForge plugin;
    private HikariDataSource dataSource;

    public SQLiteDatabase(EnchantForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() throws Exception {
        String fileName = plugin.getConfigManager().getMainConfig()
                .getString("database.sqlite.file", "enchantforge.db");
        File dbFile = new File(plugin.getDataFolder(), fileName);
        plugin.getDataFolder().mkdirs();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1); // SQLite is single-writer
        config.setConnectionTimeout(30_000);
        config.setPoolName("EnchantForge-SQLite");
        config.addDataSourceProperty("foreign_keys", "true");
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");

        dataSource = new HikariDataSource(config);
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public void createTables() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ef_player_data (
                    uuid        TEXT NOT NULL,
                    slot_key    TEXT NOT NULL,
                    enchant_data TEXT NOT NULL,
                    updated_at  INTEGER NOT NULL DEFAULT (strftime('%s','now')),
                    PRIMARY KEY (uuid, slot_key)
                )
            """);
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_ef_player_data_uuid
                ON ef_player_data(uuid)
            """);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create SQLite tables", e);
        }
    }

    @Override
    public Map<String, String> loadPlayerData(UUID uuid) {
        Map<String, String> result = new HashMap<>();
        String sql = "SELECT slot_key, enchant_data FROM ef_player_data WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("slot_key"), rs.getString("enchant_data"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load player data for " + uuid, e);
        }
        return result;
    }

    @Override
    public void savePlayerData(UUID uuid, Map<String, String> data) {
        String sql = """
            INSERT INTO ef_player_data (uuid, slot_key, enchant_data, updated_at)
            VALUES (?, ?, ?, strftime('%s','now'))
            ON CONFLICT(uuid, slot_key) DO UPDATE SET
                enchant_data = excluded.enchant_data,
                updated_at   = excluded.updated_at
        """;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, entry.getKey());
                    ps.setString(3, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save player data for " + uuid, e);
        }
    }

    @Override
    public void deletePlayerData(UUID uuid) {
        String sql = "DELETE FROM ef_player_data WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete player data for " + uuid, e);
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return dataSource != null && !dataSource.isClosed() &&
                   dataSource.getConnection().isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }
}
