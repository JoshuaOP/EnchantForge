# EnchantForge

A premium Minecraft Paper plugin providing 250+ custom enchantments with full GeyserMC/Floodgate (Bedrock) support, GUI menus, YAML-configurable enchants, scrolls, async database saving, and hooks for Vault and PlaceholderAPI.

## Run & Operate

- `pnpm --filter @workspace/api-server run dev` — run the API server (port 5000)
- `pnpm run typecheck` — full typecheck across all packages
- `pnpm run build` — typecheck + build all packages
- `pnpm --filter @workspace/api-spec run codegen` — regenerate API hooks and Zod schemas from the OpenAPI spec
- `pnpm --filter @workspace/db run push` — push DB schema changes (dev only)
- Required env: `DATABASE_URL` — Postgres connection string

### Building the Minecraft Plugin

```bash
cd EnchantForge
./gradlew shadowJar
# Output: build/libs/EnchantForge-1.0.0.jar
```

## Stack

- pnpm workspaces, Node.js 24, TypeScript 5.9
- API: Express 5
- DB: PostgreSQL + Drizzle ORM
- Validation: Zod (`zod/v4`), `drizzle-zod`
- API codegen: Orval (from OpenAPI spec)
- Build: esbuild (CJS bundle)

### Plugin Stack
- Java 21 + Paper API 1.21.4
- Gradle 8.8 (Groovy DSL) + Shadow plugin
- HikariCP for connection pooling (SQLite + MySQL)
- Adventure API + MiniMessage for text formatting
- Floodgate API (softdepend) for Bedrock player detection
- Vault API (softdepend) for economy integration
- PlaceholderAPI (softdepend) for placeholders

## Where things live

- `EnchantForge/` — Minecraft plugin project (self-contained Gradle project)
  - `src/main/java/com/joshuaop/enchantforge/` — Java source
    - `EnchantForge.java` — main plugin class, lifecycle
    - `managers/` — EnchantManager, ConfigManager, GUIManager, EffectManager, DatabaseManager, ScrollManager, AnvilManager
    - `enchants/` — CustomEnchant base, EnchantRarity/Trigger/Category enums
    - `enchants/impl/` — 10 built-in enchants (Lifesteal, VeinMiner, AutoSmelt, Thunder, Execute, ComboStrike, RocketBoost, FreezeAura, Telepathy, Explosive)
    - `enchants/yaml/` — YamlEnchant + YamlEnchantLoader for no-code enchant creation
    - `gui/` — GUI base + GUIItem abstraction
    - `gui/impl/` — EnchantBrowserGUI, CategoryMenuGUI, ScrollEditorGUI
    - `listeners/` — EnchantListener (trigger dispatch), AnvilListener, GUIListener, PlayerListener
    - `commands/` — EnchantForgeCommand (/ce with full tab-complete)
    - `database/` — Database interface, SQLiteDatabase, MySQLDatabase, DatabaseManager (cache + async flush)
    - `hooks/` — FloodgateHook, VaultHook, PlaceholderAPIHook
    - `utils/` — ColorUtils (MiniMessage), ItemBuilder, EnchantUtils (PDC), SchedulerUtils
    - `config/` — ConfigKeys constants
  - `src/main/resources/` — plugin.yml, config.yml, enchants.yml, scrolls.yml, messages.yml, rarities.yml

## Architecture decisions

- **PDC-only enchant storage**: All enchant data stored in `PersistentDataContainer` on items — no per-item database rows needed for items.
- **YAML-driven enchants**: Server owners add enchants to `enchants.yml` without any Java; `YamlEnchantLoader` parses them into `YamlEnchant` instances at runtime.
- **Trigger dispatch**: A single `EnchantListener` handles all event types and routes to registered enchants by `EnchantTrigger`. Per-player cooldown map prevents spam.
- **Async-safe DB**: All database I/O runs on the Paper async scheduler. A dirty-flag cache prevents unnecessary writes.
- **Bedrock-safe GUIs**: `FloodgateHook.isBedrockPlayer()` is checked before opening GUIs; Bedrock variants use simplified layouts to avoid inventory desync.
- **Shadow JAR**: SQLite JDBC and HikariCP are relocated under `com.joshuaop.enchantforge.libs` to avoid classpath conflicts with other plugins.

## Product

EnchantForge provides:
- 250+ custom enchants across all tool/armor categories
- YAML-based custom enchant creator (no coding required)
- Enchant books, success/destroy chance, rarity system
- Scroll system (white scroll protection, black scroll, upgrade scroll, transmog scroll)
- Enchant combining via anvil
- Bedrock-safe GUI menus (EnchantBrowser, CategoryMenu, ScrollEditor)
- Async SQLite/MySQL storage with HikariCP
- Full Floodgate + Vault + PlaceholderAPI integration
- Commands: `/ce list|info|give|book|editor|reload|create`

## User preferences

- Package: `com.joshuaop.enchantforge`

## Gotchas

- Run `./gradlew shadowJar` from the `EnchantForge/` directory, not the workspace root.
- Floodgate, Vault, and PlaceholderAPI are all softdepend — the plugin loads fine without them.
- When adding new built-in enchants, register them in `EnchantManager.registerBuiltIn()`.
- To add new YAML enchant effects, implement them in `EffectManager.applyEffect()`.

## Pointers

- See the `pnpm-workspace` skill for workspace structure, TypeScript setup, and package details
