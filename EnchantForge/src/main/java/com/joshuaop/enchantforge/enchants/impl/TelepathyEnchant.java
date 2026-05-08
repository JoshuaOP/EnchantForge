package com.joshuaop.enchantforge.enchants.impl;

import com.joshuaop.enchantforge.enchants.CustomEnchant;
import com.joshuaop.enchantforge.enchants.EnchantCategory;
import com.joshuaop.enchantforge.enchants.EnchantRarity;
import com.joshuaop.enchantforge.enchants.EnchantTrigger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;

public class TelepathyEnchant extends CustomEnchant {

    public TelepathyEnchant() {
        super(
                "telepathy",
                "<light_purple>Telepathy",
                "Sends block drops directly to your inventory.",
                EnchantRarity.RARE,
                1,
                new java.util.HashSet<>() {{
                    addAll(EnchantCategory.PICKAXE.getMaterials());
                    addAll(EnchantCategory.AXE.getMaterials());
                    addAll(EnchantCategory.SHOVEL.getMaterials());
                    addAll(EnchantCategory.HOE.getMaterials());
                }},
                EnchantTrigger.BLOCK_BREAK,
                100.0,
                0.0,
                0L
        );
    }

    @Override
    public void apply(Player player, int level, Event event) {
        if (!(event instanceof BlockBreakEvent bbe)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        Collection<ItemStack> drops = bbe.getBlock().getDrops(tool, player);

        bbe.setDropItems(false);

        for (ItemStack drop : drops) {
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(drop);
            // Drop anything that didn't fit
            leftovers.values().forEach(leftover ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }
    }
}
