package org.pronze.hypixelify.utils;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.pronze.hypixelify.Configurator;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.listener.PlayerListener;
import org.pronze.hypixelify.listener.Shop;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.TeamColor;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.utils.ColorChanger;
import org.screamingsandals.bedwars.game.GameCreator;
import org.screamingsandals.bedwars.lib.sgui.builder.FormatBuilder;
import org.screamingsandals.bedwars.lib.sgui.inventory.Options;

import java.util.*;

import static org.screamingsandals.bedwars.lib.lang.I.i18n;

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
        Map<String, Object> options = new HashMap<>();
        options.put("rows", 6);
        options.put("render_actual_rows", 6);


        builder.add(category)
                .set("column", 3)
                .set("row", 1);
        builder.add(category2)
                .set("row", 1)
                .set("column", 5)
                .set("items", games)
                .set("options", options);
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

    public static ArrayList<Object> createGamesGUI(int mode, List<String> lore){
        ArrayList<Object> games = new ArrayList<>();

        ItemStack air = new ItemStack(Material.AIR);
        HashMap<String, Object> tempmappings1 = new HashMap<>();
        tempmappings1.put("stack", air);
        games.add(tempmappings1);
        for (org.screamingsandals.bedwars.api.game.Game game : BedwarsAPI.getInstance()
                .getGames()) {
            if (Configurator.game_size.containsKey(game.getName()) && Configurator.game_size.get(game.getName()).equals(mode)) {
                ItemStack temp = new ItemStack(Material.PAPER);
                ItemMeta meta1 = temp.getItemMeta();
                String name1 = ChatColor.GREEN + game.getName();
                List<String> newLore = new ArrayList<>();
                for (String ls : lore){
                    String l =ls.replace("{players}", String.valueOf(game.getConnectedPlayers().size()))
                                .replace("{status}", Shop.capFirstLetter(game.getStatus().name()));
                    newLore.add(l);
                }
                meta1.setLore(newLore);
                meta1.setDisplayName(name1);
                temp.setItemMeta(meta1);
                HashMap<String, Object> tempmappings = new HashMap<>();
                tempmappings.put("stack", temp);
                tempmappings.put("game", game);
                games.add(tempmappings);
            }
        }

        ItemStack arrowStack = new ItemStack(Material.ARROW);
        ItemMeta metaArrow = arrowStack.getItemMeta();
        metaArrow.setDisplayName("§aGo Back");
        metaArrow.setLore(Arrays.asList("§7To Play Bed Wars"));
        arrowStack.setItemMeta(metaArrow);
        HashMap<String, Object> arrows = new HashMap<>();
        arrows.put("stack", arrowStack);
        arrows.put("row", 5);
        arrows.put("column", 4);
        arrows.put("locate", "main");
        games.add(arrows);
        return games;
    }

    public static Options generateOptions(){
        Options options = new Options(Hypixelify.getInstance());
        options.setShowPageNumber(false);

        ItemStack backItem = Main.getConfigurator().readDefinedItem("shopback", "BARRIER");
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName(i18n("shop_back", false));
        backItem.setItemMeta(backItemMeta);
        options.setBackItem(backItem);

        ItemStack pageBackItem = Main.getConfigurator().readDefinedItem("pageback", "ARROW");
        ItemMeta pageBackItemMeta = backItem.getItemMeta();
        pageBackItemMeta.setDisplayName(i18n("page_back", false));
        pageBackItem.setItemMeta(pageBackItemMeta);
        options.setPageBackItem(pageBackItem);

        ItemStack pageForwardItem = Main.getConfigurator().readDefinedItem("pageforward", "ARROW");
        ItemMeta pageForwardItemMeta = backItem.getItemMeta();
        pageForwardItemMeta.setDisplayName(i18n("page_forward", false));
        pageForwardItem.setItemMeta(pageForwardItemMeta);
        options.setPageForwardItem(pageForwardItem);

        ItemStack cosmeticItem = Main.getConfigurator().readDefinedItem("shopcosmetic", "AIR");
        options.setCosmeticItem(cosmeticItem);
        options.setRender_header_start(600);
        options.setRender_footer_start(600);
        options.setRender_offset(9);
        options.setRows(4);
        options.setRender_actual_rows(4);
        options.setShowPageNumber(false);

        return options;
    }

    public static List<ItemStack> createCategories(List<String> lore1,
                                            String name, String name2){
        List<ItemStack> myList = new ArrayList<>();
        ItemStack category = new ItemStack(Material.valueOf("RED_BED"));
        ItemStack category2 = new ItemStack(Material.OAK_SIGN);
        ItemStack category3 = new ItemStack(Material.BARRIER);
        ItemStack category4 = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = category.getItemMeta();
        meta.setLore(lore1);
        meta.setDisplayName(name);
        category.setItemMeta(meta);

        ItemMeta meta2 = category2.getItemMeta();
        meta2.setLore(Arrays.asList("§7Pick which map you want to play", "§7from a list of available servers.", " "
                , "§eClick to browse!"));
        meta2.setDisplayName(name2);
        category2.setItemMeta(meta2);

        ItemMeta meta3 = category3.getItemMeta();
        String name3 = "§cExit";
        meta3.setDisplayName(name3);
        category3.setItemMeta(meta3);

        ItemMeta meta4 = category4.getItemMeta();
        String name4 = "§cClick here to rejoin!";
        meta4.setLore(Arrays.asList("§7Click here to rejoin the lastly joined game"));
        meta4.setDisplayName(name4);
        category4.setItemMeta(meta4);;

        myList.add(category);
        myList.add(category2);
        myList.add(category3);
        myList.add(category4);

        return myList;
    }
    public static String translateColors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }


}
