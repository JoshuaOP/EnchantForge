package com.joshuaop.enchantforge.enchants.impl;

import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantCategory;
import com.joshuaop.enchantforge.enchants.EnchantRarity;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import com.joshuaop.enchantforge.utils.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class VeinMinerEnchant extends CustomEnchant {

    private static final int MAX_BLOCKS = 64;

    private static final Set<Material> ORE_MATERIALS = Set.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
            Material.ANCIENT_DEBRIS
    );

    public VeinMinerEnchant() {
        super(
                "veinminer",
                "<gold>VeinMiner",
                "Mine entire ore veins at once.",
                EnchantRarity.LEGENDARY,
                5,
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
        Block origin = bbe.getBlock();
        Material type = origin.getType();
        if (!ORE_MATERIALS.contains(type)) return;
        if (!player.isSneaking()) return; // require sneak to activate

        int maxBlocks = 8 + (level * 8); // 16 - 48 blocks depending on level
        maxBlocks = Math.min(maxBlocks, MAX_BLOCKS);

        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty() && visited.size() < maxBlocks) {
            Block current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!visited.contains(neighbor) && neighbor.getType() == type) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        // Remove the origin (already broken by the event)
        visited.remove(origin);

        ItemStack tool = player.getInventory().getItemInMainHand();
        for (Block block : visited) {
            block.getDrops(tool).forEach(drop ->
                    block.getWorld().dropItemNaturally(block.getLocation(), drop));
            block.setType(Material.AIR);
        }
    }
}
