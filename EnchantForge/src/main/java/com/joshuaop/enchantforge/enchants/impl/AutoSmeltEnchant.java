package com.joshuaop.enchantforge.enchants.impl;

import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantCategory;
import com.joshuaop.enchantforge.enchants.EnchantRarity;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AutoSmeltEnchant extends CustomEnchant {

    private static final Map<Material, Material> SMELT_MAP = Map.ofEntries(
            Map.entry(Material.IRON_ORE, Material.IRON_INGOT),
            Map.entry(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT),
            Map.entry(Material.GOLD_ORE, Material.GOLD_INGOT),
            Map.entry(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT),
            Map.entry(Material.COPPER_ORE, Material.COPPER_INGOT),
            Map.entry(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT),
            Map.entry(Material.NETHER_GOLD_ORE, Material.GOLD_NUGGET),
            Map.entry(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP),
            Map.entry(Material.SAND, Material.GLASS),
            Map.entry(Material.GRAVEL, Material.FLINT),
            Map.entry(Material.COBBLESTONE, Material.STONE),
            Map.entry(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE),
            Map.entry(Material.CLAY, Material.TERRACOTTA)
    );

    public AutoSmeltEnchant() {
        super(
                "autosmelt",
                "<orange>AutoSmelt",
                "Smelts blocks instantly when mined.",
                EnchantRarity.RARE,
                1,
                EnchantCategory.PICKAXE.getMaterials(),
                EnchantTrigger.BLOCK_BREAK,
                100.0,
                0.0,
                0L
        );
    }

    @Override
    public void apply(Player player, int level, Event event) {
        if (!(event instanceof BlockBreakEvent bbe)) return;
        Material broken = bbe.getBlock().getType();
        Material smelted = SMELT_MAP.get(broken);
        if (smelted == null) return;

        // Cancel normal drops and give smelted result
        bbe.setDropItems(false);
        player.getWorld().dropItemNaturally(
                bbe.getBlock().getLocation(),
                new ItemStack(smelted)
        );

        player.getWorld().spawnParticle(
                org.bukkit.Particle.FLAME,
                bbe.getBlock().getLocation().add(0.5, 0.5, 0.5),
                8, 0.2, 0.2, 0.2, 0.02
        );
    }
}
