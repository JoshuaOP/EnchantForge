package com.joshuaop.enchantforge.config;

/**
 * Centralized constants for configuration key paths.
 * Prevents typo-driven bugs when accessing nested config values.
 */
public final class ConfigKeys {

    private ConfigKeys() {}

    // ── Settings ──────────────────────────────────────────────

    public static final String DEBUG              = "settings.debug";
    public static final String UPDATE_CHECK       = "settings.update-check";
    public static final String METRICS            = "settings.metrics";

    // ── Enchants ──────────────────────────────────────────────

    public static final String MAX_PER_ITEM       = "enchants.max-per-item";
    public static final String SHOW_LORE          = "enchants.show-lore";
    public static final String LORE_FORMAT        = "enchants.lore-format";
    public static final String ANIMATION_TICK     = "enchants.animation-tick-rate";

    // ── Combining ─────────────────────────────────────────────

    public static final String COMBINING_ENABLED  = "combining.enabled";
    public static final String XP_COST_PER_ENCHANT= "combining.xp-cost-per-enchant";
    public static final String COMBINING_MAX_CAP  = "combining.max-level-cap";

    // ── Scrolls ───────────────────────────────────────────────

    public static final String SCROLLS_ENABLED    = "scrolls.enabled";

    // ── Books ─────────────────────────────────────────────────

    public static final String BOOKS_ENABLED      = "books.enabled";
    public static final String BOOKS_MATERIAL     = "books.material";
    public static final String BOOKS_GLOW         = "books.glow";

    // ── GUI ───────────────────────────────────────────────────

    public static final String GUI_ENCHANTS_PER_PAGE  = "gui.enchants-per-page";
    public static final String GUI_UPDATE_TICKS        = "gui.update-ticks";
    public static final String GUI_BEDROCK_ROWS        = "gui.bedrock.rows";

    // ── Database ──────────────────────────────────────────────

    public static final String DB_TYPE            = "database.type";
    public static final String DB_SQLITE_FILE     = "database.sqlite.file";
    public static final String DB_MYSQL_HOST      = "database.mysql.host";
    public static final String DB_MYSQL_PORT      = "database.mysql.port";
    public static final String DB_MYSQL_DATABASE  = "database.mysql.database";
    public static final String DB_MYSQL_USER      = "database.mysql.username";
    public static final String DB_MYSQL_PASSWORD  = "database.mysql.password";
    public static final String DB_SAVE_INTERVAL   = "database.save-interval";

    // ── Hooks ─────────────────────────────────────────────────

    public static final String HOOK_PAPI          = "hooks.placeholderapi.enabled";
    public static final String HOOK_VAULT         = "hooks.vault.enabled";
    public static final String HOOK_FLOODGATE     = "hooks.floodgate.enabled";
    public static final String HOOK_GEYSER        = "hooks.geyser.enabled";
}
