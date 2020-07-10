package org.pronze.hypixelify.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.pronze.hypixelify.listener.PlayerListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ShopUtil {

    public static void addEnchantsToPlayerArmor(Player player, ItemStack item) {
        for (ItemStack i : player.getInventory().getArmorContents()) {
            if (i != null) {
                i.addEnchantments(item.getEnchantments());
            }
        }
    }

    public static void buyArmor(Player player, Material mat_boots, Material mat_leggings) {
        ItemStack boots = new ItemStack(mat_boots);
        boots.addEnchantments(Objects.requireNonNull(player.getInventory().getBoots()).getEnchantments());
        ItemStack leggings = new ItemStack(mat_leggings);
        leggings.addEnchantments(player.getInventory().getBoots().getEnchantments());
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        player.getInventory().setBoots(boots);
        player.getInventory().setLeggings(leggings);
    }

    public static boolean addEnchantsToPlayerTools(Player player, ItemStack newItem, String name, Enchantment enchantment) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().name().endsWith(name)) {
                if (item.getEnchantmentLevel(enchantment) >= newItem.getEnchantmentLevel(enchantment) || newItem.getEnchantmentLevel(enchantment) >= 5)
                    return false;

                item.addEnchantments(newItem.getEnchantments());
            }
        }
        return true;
    }

    public static ItemStack shopEnchants(ItemStack sh_item, ItemStack pl_Item, Enchantment enchant) {
        int sz = pl_Item.getEnchantmentLevel(enchant);
        if (sz >= 0 && sz < 4) {
            sh_item.addEnchantment(enchant, sz + 1);
        } else if (sz == 4) {
            sh_item.removeEnchantment(enchant);
            sh_item.setLore(Arrays.asList("Maximum Enchant", "Your team already has maximum Enchant."));
        }
        return sh_item;
    }
}
