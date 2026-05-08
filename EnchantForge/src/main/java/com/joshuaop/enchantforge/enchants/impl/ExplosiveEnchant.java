package com.joshuaop.enchantforge.enchants.impl;

import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantCategory;
import com.joshuaop.enchantforge.enchants.EnchantRarity;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ExplosiveEnchant extends CustomEnchant {

    public ExplosiveEnchant() {
        super(
                "explosive",
                "<red>Explosive",
                "Mines blocks in an area around the target.",
                EnchantRarity.EPIC,
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
        if (player.isSneaking()) return; // hold sneak to disable

        Block origin = bbe.getBlock();
        int radius = level; // 1-5 block radius
        ItemStack tool = player.getInventory().getItemInMainHand();

        List<Block> toBreak = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    Block b = origin.getRelative(dx, dy, dz);
                    if (b.getType() == Material.AIR || b.getType() == Material.BEDROCK) continue;
                    toBreak.add(b);
                }
            }
        }

        for (Block b : toBreak) {
            b.getDrops(tool, player).forEach(drop ->
                    b.getWorld().dropItemNaturally(b.getLocation(), drop));
            b.setType(Material.AIR);
        }

        origin.getWorld().spawnParticle(
                org.bukkit.Particle.EXPLOSION,
                origin.getLocation().add(0.5, 0.5, 0.5),
                3, radius * 0.3, radius * 0.3, radius * 0.3, 0
        );
        origin.getWorld().playSound(
                origin.getLocation(),
                org.bukkit.Sound.ENTITY_GENERIC_EXPLODE,
                0.6f, 1.5f
        );
    }
}
