package com.joshuaop.enchantforge.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class ColorUtils {

    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private ColorUtils() {}

    /** Parse a MiniMessage or legacy &-color string into a Component. */
    public static Component parse(String text) {
        if (text == null) return Component.empty();
        // Try legacy first (contains & codes), then MiniMessage
        if (text.contains("&")) {
            String mm = text.replace("&", "§");
            // Convert §-codes to MiniMessage tags for full Adventure support
            text = LEGACY.serialize(LegacyComponentSerializer.legacySection().deserialize(mm));
        }
        return MINI_MESSAGE.deserialize(text);
    }

    /** Parse with placeholder replacement for a player. */
    public static Component parse(String text, Player player) {
        if (text == null) return Component.empty();
        text = text.replace("{player}", player.getName());
        return parse(text);
    }

    /** Parse a list of strings into Components. */
    public static List<Component> parseList(List<String> lines) {
        return lines.stream().map(ColorUtils::parse).collect(Collectors.toList());
    }

    /** Strip all color/format tags and return plain text. */
    public static String strip(String text) {
        if (text == null) return "";
        return MINI_MESSAGE.stripTags(text);
    }

    /** Convert a Component back to a legacy string (for compatibility layers). */
    public static String toLegacy(Component component) {
        return LEGACY.serialize(component);
    }

    /** Roman numeral helper for enchant level display. */
    public static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }
}
