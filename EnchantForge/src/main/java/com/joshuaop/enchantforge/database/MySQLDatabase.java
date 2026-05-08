package com.joshuaop.enchantforge.database;

import com.joshuaop.enchantforge.EnchantForge;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MySQLDatabase implements Database {

    private final EnchantForge plugin;
    private HikariDataSource dataSource;

    public MySQLDatabase(EnchantForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() throws Exception {
        FileConfiguration cfg = plugin.getConfigManager().getMainConfig();
        String host     = cfg.getString("database.mysql.host", "localhost");
        int    port     = cfg.getInt   ("database.mysql.port", 3306);
        String db       = cfg.getString("database.mysql.database", "enchantforge");
        String user     = cfg.getString("database.mysql.username", "root");
        String password = cfg.getString("database.mysql.password", "");
        int    poolSize = cfg.getInt   ("database.mysql.pool-size", 10);
        long   connTimeout  = cfg.getLong("database.mysql.connection-timeout", 30_000L);
        long   idleTimeout  = cfg.getLong("database.mysql.idle-timeout", 600_000L);
        long   maxLifetime  = cfg.getLong("database.mysql.max-lifetime", 1_800_000L);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port, db));
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setConnectionTimeout(connTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setPoolName("EnchantForge-MySQL");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

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
        String sql = """
            CREATE TABLE IF NOT EXISTS ef_player_data (
                uuid         VARCHAR(36)  NOT NULL,
                slot_key     VARCHAR(64)  NOT NULL,
                enchant_data TEXT         NOT NULL,
                updated_at   BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP()),
                PRIMARY KEY (uuid, slot_key),
                INDEX idx_uuid (uuid)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create MySQL tables", e);
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
            plugin.getLogger().log(Level.WARNING, "Failed to load MySQL data for " + uuid, e);
        }
        return result;
    }

    @Override
    public void savePlayerData(UUID uuid, Map<String, String> data) {
        String sql = """
            INSERT INTO ef_player_data (uuid, slot_key, enchant_data, updated_at)
            VALUES (?, ?, ?, UNIX_TIMESTAMP())
            ON DUPLICATE KEY UPDATE
                enchant_data = VALUES(enchant_data),
                updated_at   = VALUES(updated_at)
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
            plugin.getLogger().log(Level.WARNING, "Failed to save MySQL data for " + uuid, e);
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
            plugin.getLogger().log(Level.WARNING, "Failed to delete MySQL data for " + uuid, e);
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return dataSource != null && !dataSource.isClosed() &&
                   dataSource.getConnection().isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
