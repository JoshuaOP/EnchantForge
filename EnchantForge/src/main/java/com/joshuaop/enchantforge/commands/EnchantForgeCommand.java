package com.joshuaop.enchantforge.commands;

import com.joshuaop.enchantforge.EnchantForge;
import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantRarity;
import com.joshuaop.enchantforge.utils.ColorUtils;
import com.joshuaop.enchantforge.utils.EnchantUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantForgeCommand implements CommandExecutor, TabCompleter {

    private final EnchantForge plugin;

    public EnchantForgeCommand(EnchantForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "help"   -> { sendHelp(sender); yield true; }
            case "list"   -> { handleList(sender, args); yield true; }
            case "info"   -> { handleInfo(sender, args); yield true; }
            case "give"   -> { handleGive(sender, args); yield true; }
            case "book"   -> { handleBook(sender, args); yield true; }
            case "editor" -> { handleEditor(sender, args); yield true; }
            case "reload" -> { handleReload(sender); yield true; }
            case "create" -> { handleCreate(sender, args); yield true; }
            default -> { sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.unknown-command"))); yield true; }
        };
    }

    // ── /ce help ──────────────────────────────────────────────

    private void sendHelp(CommandSender sender) {
        var cfg = plugin.getConfigManager().getMessagesConfig();
        sender.sendMessage(ColorUtils.parse(cfg.getString("help.header", "")));
        for (String line : cfg.getStringList("help.commands")) {
            sender.sendMessage(ColorUtils.parse(line));
        }
        sender.sendMessage(ColorUtils.parse(cfg.getString("help.footer", "")));
    }

    // ── /ce list [page] ───────────────────────────────────────

    private void handleList(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.player-only")));
            return;
        }
        int page = 1;
        if (args.length > 1) {
            try { page = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
        }
        plugin.getGUIManager().openEnchantBrowser(player, page);
    }

    // ── /ce info <enchant> ────────────────────────────────────

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.parse("<red>Usage: /ce info <enchant>"));
            return;
        }
        CustomEnchant ce = plugin.getEnchantManager().getEnchant(args[1]);
        if (ce == null) {
            sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.invalid-enchant")
                          .replace("{enchant}", args[1])));
            return;
        }
        sender.sendMessage(ColorUtils.parse(""));
        sender.sendMessage(ColorUtils.parse("<gradient:#FF6B35:#FFD700>━━━━ Enchant Info ━━━━</gradient>"));
        sender.sendMessage(ColorUtils.parse("<gray>Name: <white>" + ce.getDisplayName()));
        sender.sendMessage(ColorUtils.parse("<gray>ID: <white>" + ce.getId()));
        sender.sendMessage(ColorUtils.parse("<gray>Rarity: <white>" + ce.getRarity().name()));
        sender.sendMessage(ColorUtils.parse("<gray>Max Level: <white>" + ce.getMaxLevel()));
        sender.sendMessage(ColorUtils.parse("<gray>Trigger: <white>" + ce.getTrigger().name()));
        sender.sendMessage(ColorUtils.parse("<gray>Chance: <white>" + ce.getBaseChance() + "%"));
        if (ce.getCooldownSeconds() > 0) {
            sender.sendMessage(ColorUtils.parse("<gray>Cooldown: <white>" + ce.getCooldownSeconds() + "s"));
        }
        sender.sendMessage(ColorUtils.parse("<gray>Description: <white>" + ce.getDescription()));
        sender.sendMessage(ColorUtils.parse("<gradient:#FF6B35:#FFD700>━━━━━━━━━━━━━━━━━━━━━━</gradient>"));
    }

    // ── /ce give <player> <enchant> [level] [amount] ──────────

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("enchantforge.give")) {
            sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.no-permission")));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(ColorUtils.parse("<red>Usage: /ce give <player> <enchant> [level] [amount]"));
            return;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.invalid-player")
                          .replace("{player}", args[1])));
            return;
        }
        CustomEnchant ce = plugin.getEnchantManager().getEnchant(args[2]);
        if (ce == null) {
            sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.invalid-enchant")
                          .replace("{enchant}", args[2])));
            return;
        }
        int level = args.length > 3 ? Math.max(1, Math.min(Integer.parseInt(args[3]), ce.getMaxLevel())) : 1;
        int amount = args.length > 4 ? Math.max(1, Integer.parseInt(args[4])) : 1;

        ItemStack book = plugin.getEnchantManager().createEnchantBook(ce, level);
        book.setAmount(amount);
        target.getInventory().addItem(book);

        sender.sendMessage(ColorUtils.parse(
                plugin.getConfigManager().getMessage("enchants.give-success")
                      .replace("{amount}", String.valueOf(amount))
                      .replace("{enchant}", ce.getDisplayName())
                      .replace("{level}", ColorUtils.toRoman(level))
                      .replace("{player}", target.getName())));
        target.sendMessage(ColorUtils.parse(
                plugin.getConfigManager().getMessage("enchants.give-received")
                      .replace("{amount}", String.valueOf(amount))
                      .replace("{enchant}", ce.getDisplayName())
                      .replace("{level}", ColorUtils.toRoman(level))));
    }

    // ── /ce book <enchant> [level] ────────────────────────────

    private void handleBook(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.player-only")));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.parse("<red>Usage: /ce book <enchant> [level]"));
            return;
        }
        CustomEnchant ce = plugin.getEnchantManager().getEnchant(args[1]);
        if (ce == null) {
            sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.invalid-enchant")
                          .replace("{enchant}", args[1])));
            return;
        }
        int level = args.length > 2 ? Math.max(1, Math.min(Integer.parseInt(args[2]), ce.getMaxLevel())) : 1;
        player.getInventory().addItem(plugin.getEnchantManager().createEnchantBook(ce, level));
        player.sendMessage(ColorUtils.parse(
                plugin.getConfigManager().getMessage("enchants.give-received")
                      .replace("{amount}", "1")
                      .replace("{enchant}", ce.getDisplayName())
                      .replace("{level}", ColorUtils.toRoman(level))));
    }

    // ── /ce editor ────────────────────────────────────────────

    private void handleEditor(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.player-only")));
            return;
        }
        if (!sender.hasPermission("enchantforge.editor")) {
            sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.no-permission")));
            return;
        }
        plugin.getGUIManager().openScrollEditor(player);
    }

    // ── /ce reload ────────────────────────────────────────────

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("enchantforge.reload")) {
            sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.no-permission")));
            return;
        }
        plugin.reload();
        sender.sendMessage(ColorUtils.parse(
                plugin.getConfigManager().getMessage("general.reload-success")));
    }

    // ── /ce create <name> ─────────────────────────────────────

    private void handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("enchantforge.create")) {
            sender.sendMessage(ColorUtils.parse(
                    plugin.getConfigManager().getMessage("general.no-permission")));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.parse("<red>Usage: /ce create <name>"));
            return;
        }
        String name = args[1].toLowerCase().replace(" ", "_");

        // Write a template entry to enchants.yml
        var enchantsConfig = plugin.getConfigManager().getEnchantsConfig();
        String path = "custom-enchants." + name;
        if (enchantsConfig.contains(path)) {
            sender.sendMessage(ColorUtils.parse("<red>An enchant with that name already exists."));
            return;
        }
        enchantsConfig.set(path + ".display", "&e" + name);
        enchantsConfig.set(path + ".description", List.of("A custom enchant."));
        enchantsConfig.set(path + ".item-types", List.of("SWORD"));
        enchantsConfig.set(path + ".rarity", "COMMON");
        enchantsConfig.set(path + ".max-level", 5);
        enchantsConfig.set(path + ".trigger", "ATTACK");
        enchantsConfig.set(path + ".chance", 20.0);
        enchantsConfig.set(path + ".effects", List.of("LIGHTNING"));
        plugin.getConfigManager().saveCustomConfig(enchantsConfig, "enchants.yml");
        plugin.reload();

        sender.sendMessage(ColorUtils.parse(
                "<green>Created custom enchant template '<yellow>" + name + "</yellow>'." +
                " Edit <gray>enchants.yml</gray> to configure it."));
    }

    // ── Tab completion ─────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String label, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(
                    List.of("help", "list", "info", "give", "book", "editor", "reload", "create"),
                    args[0]);
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "info", "book" -> filterStartsWith(
                        plugin.getEnchantManager().getAllEnchants().stream()
                              .map(CustomEnchant::getId).collect(Collectors.toList()),
                        args[1]);
                case "give" -> filterStartsWith(
                        plugin.getServer().getOnlinePlayers().stream()
                              .map(Player::getName).collect(Collectors.toList()),
                        args[1]);
                default -> List.of();
            };
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return filterStartsWith(
                    plugin.getEnchantManager().getAllEnchants().stream()
                          .map(CustomEnchant::getId).collect(Collectors.toList()),
                    args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            CustomEnchant ce = plugin.getEnchantManager().getEnchant(args[2]);
            if (ce != null) {
                List<String> levels = new ArrayList<>();
                for (int i = 1; i <= ce.getMaxLevel(); i++) levels.add(String.valueOf(i));
                return filterStartsWith(levels, args[3]);
            }
        }
        return List.of();
    }

    private List<String> filterStartsWith(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
