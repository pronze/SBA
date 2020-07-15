package org.pronze.hypixelify.utils;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.pronze.hypixelify.Configurator;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.listener.PlayerListener;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.TeamColor;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.utils.ColorChanger;
import org.screamingsandals.bedwars.game.GameCreator;
import org.screamingsandals.bedwars.lib.sgui.builder.FormatBuilder;

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

    public static void destroyNPCFromGameWorlds(){
        List<NPC> npcs = new ArrayList<>();
        for(Game game: Main.getInstance().getGames()){
            CitizensAPI.getNPCRegistry().forEach(npc -> {
                if(GameCreator.isInArea(npc.getStoredLocation(), game.getPos1(), game.getPos2())){
                    npcs.add(npc);
                }
            });
        }
        if(!npcs.isEmpty()){
            for(NPC npc : npcs){
                npc.destroy();
            }
        }
    }

    public static <K, V> K getKey(HashMap<K, V> map, V value) {
        for (K key : map.keySet()) {
            if (value.equals(map.get(key))) {
                return key;
            }
        }
        return null;
    }

    public static void initalizekeys(){
        PlayerListener.UpgradeKeys.put("WOODEN", 1);
        PlayerListener.UpgradeKeys.put("STONE", 2);
        PlayerListener.UpgradeKeys.put("GOLDEN", 3);
        PlayerListener.UpgradeKeys.put("IRON", 4);
        PlayerListener.UpgradeKeys.put("DIAMOND", 5);

        for (String material : Hypixelify.getConfigurator().config.getStringList("allowed-item-drops")) {
            Material mat;
            try {
                mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
            } catch (Exception ignored) {
                continue;
            }
            PlayerListener.allowed.add(mat);
        }
        for (String material : Hypixelify.getConfigurator().config.getStringList("running-generator-drops")) {
            Material mat;
            try {
                mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
            } catch (Exception ignored) {
                continue;
            }
            PlayerListener.generatorDropItems.add(mat);
        }
    }

    public static void giveItemToPlayer(List<ItemStack> itemStackList, Player player, TeamColor teamColor) {
        for (ItemStack itemStack : itemStackList) {
            if (!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)) return;

            ColorChanger colorChanger = BedwarsAPI.getInstance().getColorChanger();

            final String materialName = itemStack.getType().toString();
            final PlayerInventory playerInventory = player.getInventory();

            if (materialName.contains("HELMET")) {
                playerInventory.setHelmet(colorChanger.applyColor(teamColor, itemStack));
            } else if (materialName.contains("CHESTPLATE")) {
                playerInventory.setChestplate(colorChanger.applyColor(teamColor, itemStack));
            } else if (materialName.contains("LEGGINGS")) {
                playerInventory.setLeggings(colorChanger.applyColor(teamColor, itemStack));
            } else if (materialName.contains("BOOTS")) {
                playerInventory.setBoots(colorChanger.applyColor(teamColor, itemStack));
            } else if (materialName.contains("PICKAXE")) {
                playerInventory.setItem(7, itemStack);
            } else if (materialName.contains("AXE")) {
                playerInventory.setItem(8, itemStack);
            } else if (materialName.contains("SWORD")) {
                playerInventory.setItem(0, itemStack);
            } else {
                playerInventory.addItem(colorChanger.applyColor(teamColor, itemStack));
            }
        }
    }

    public static ItemStack checkifUpgraded(ItemStack newItem) {
        if (PlayerListener.UpgradeKeys.get(newItem.getType().name().substring(0, newItem.getType().name().indexOf("_"))) > PlayerListener.UpgradeKeys.get("WOODEN")) {
            Map<Enchantment, Integer> enchant = newItem.getEnchantments();
            Material mat;
            mat = Material.valueOf(ShopUtil.getKey(PlayerListener.UpgradeKeys, PlayerListener.UpgradeKeys.get(newItem.getType().name().substring(0, newItem.getType().name().indexOf("_"))) - 1) + newItem.getType().name().substring(newItem.getType().name().lastIndexOf("_")));
            ItemStack temp = new ItemStack(mat);
            temp.addEnchantments(enchant);
            return temp;
        }
        return newItem;
    }



    public static String translateColors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }


}
