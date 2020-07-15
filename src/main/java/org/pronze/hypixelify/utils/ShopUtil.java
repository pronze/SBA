package org.pronze.hypixelify.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.pronze.hypixelify.Configurator;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.lib.sgui.builder.FormatBuilder;
import org.screamingsandals.bedwars.lib.sgui.events.PostActionEvent;

import java.util.*;

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

    static <K, V> List<K> getAllKeysForValue(Map<K, V> mapOfWords, V value)
    {
        List<K> listOfKeys = null;
        if(mapOfWords.containsValue(value))
        {
            listOfKeys = new ArrayList<>();

            for (Map.Entry<K, V> entry : mapOfWords.entrySet())
            {
                if (entry.getValue().equals(value))
                {
                    listOfKeys.add(entry.getKey());
                }
            }
        }
        return listOfKeys;
    }

    public static List<Game> getGamesWithSize(int c){
        List<String> allmapnames = getAllKeysForValue(Configurator.game_size, c);
        if(allmapnames == null || allmapnames.isEmpty())
            return null;

        ArrayList<Game> listofgames = new ArrayList<>();

        for(String n : allmapnames){
            if(Main.getGameNames().contains(n)){
                listofgames.add(Main.getGame(n));
            }
        }

        return listofgames;
    }

    public static FormatBuilder createBuilder(ArrayList<Object> games, ItemStack category, ItemStack category2, ItemStack category3,
                                       ItemStack category4){
        FormatBuilder builder = new FormatBuilder();
        builder.add(category)
                .set("column", 3)
                .set("row", 1);
        builder.add(category2)
                .set("row", 1)
                .set("column", 5)
                .set("items", games);
        builder.add(category3)
                .set("row", 3)
                .set("column", 4);
        builder.add(category4)
                .set("row",3)
                .set("column", 8);

        return builder;
    }



}
