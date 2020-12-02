package io.pronze.hypixelify.utils;

import io.pronze.hypixelify.Configurator;
import io.pronze.hypixelify.SBAHypixelify;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import io.pronze.hypixelify.api.wrapper.PlayerWrapper;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.TeamColor;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.utils.ColorChanger;
import org.screamingsandals.bedwars.lib.sgui.builder.FormatBuilder;
import org.screamingsandals.bedwars.lib.sgui.inventory.Options;

import java.util.*;

import static org.screamingsandals.bedwars.lib.lang.I.i18n;

public class ShopUtil {

    private final static Map<String, Integer> UpgradeKeys = new HashMap<>();
    public static ItemStack Diamond, FireWorks, Arrow, BED;

    private static void InitalizeStacks() {

        UpgradeKeys.clear();
        UpgradeKeys.put("STONE", 2);
        UpgradeKeys.put("IRON", 4);
        UpgradeKeys.put("DIAMOND", 5);
        if (!Main.isLegacy()) {
            UpgradeKeys.put("WOODEN", 1);
            UpgradeKeys.put("GOLDEN", 3);
        } else {
            UpgradeKeys.put("WOOD", 1);
            UpgradeKeys.put("GOLD", 3);
        }

        Arrow = new ItemStack(Material.ARROW);
        final ItemMeta metaArrow = Arrow.getItemMeta();

        metaArrow.setDisplayName(SBAHypixelify.getConfigurator()
                .config.getString("games-inventory.back-item.name", "§aGo Back"));
        final List<String> arrowLore = SBAHypixelify.getConfigurator()
                .config.getStringList("games-inventory.back-item.lore");

        metaArrow.setLore(arrowLore);
        Arrow.setItemMeta(metaArrow);

        if (Main.isLegacy()) {
            FireWorks = new ItemStack(Material.valueOf("FIREWORK"));
            BED = new ItemStack(Material.valueOf("BED"));
        } else {
            FireWorks = new ItemStack(Material.FIREWORK_ROCKET);
            BED = new ItemStack(Material.RED_BED);
        }

        ItemMeta fireMeta = FireWorks.getItemMeta();
        fireMeta.setDisplayName(SBAHypixelify.getConfigurator()
                .config.getString("games-inventory.firework-name", "§aRandom Map"));
        FireWorks.setItemMeta(fireMeta);

        Diamond = new ItemStack(Material.DIAMOND);
        ItemMeta diamondMeta = Diamond.getItemMeta();
        diamondMeta.setDisplayName(SBAHypixelify.getConfigurator()
                .config.getString("games-inventory.firework-name", "§aRandom Favorite"));
        Diamond.setItemMeta(diamondMeta);
    }

    public static void addEnchantsToPlayerArmor(Player player, ItemStack newItem) {
        Arrays.stream(player.getInventory().getArmorContents()).forEach(item ->{
            if(item != null){
                item.addEnchantments(newItem.getEnchantments());
            }
        });
    }

    public static void buyArmor(Player player, Material mat_boots, String name, Game game) {
        String matName = name.substring(0, name.indexOf("_"));
        Material mat_leggings = Material.valueOf(matName + "_LEGGINGS");
        ItemStack boots = new ItemStack(mat_boots);
        ItemStack leggings = new ItemStack(mat_leggings);
        int level = 0;
        try {
            level = Objects.requireNonNull(SBAHypixelify.getGamestorage(game)).getProtection(game.getTeamOfPlayer(player).getName());
        } catch (Throwable ignored){

        }

        if (level != 0) {
            boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
            leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
        }
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        player.getInventory().setBoots(boots);
        player.getInventory().setLeggings(leggings);
    }

    public static boolean addEnchantsToPlayerTools(Player buyer, ItemStack newItem, String name, Enchantment enchantment) {
        final int newItemEnchantLevel = newItem.getEnchantmentLevel(enchantment);

        for (ItemStack item : buyer.getInventory().getContents()) {
            if(item == null) continue;

            final String typeName = item.getType().name();
            final int itemEnchantLevel = item.getEnchantmentLevel(enchantment);
            if (typeName.endsWith(name)) {
                if (itemEnchantLevel >= newItemEnchantLevel || newItemEnchantLevel >= 5)
                    return false;

                item.addEnchantments(newItem.getEnchantments());
            }
        }

        return true;
    }

    public static boolean addEnchantsToTeamTools(Player buyer, ItemStack stack, String name, Enchantment enchantment) {
        RunningTeam team = BedwarsAPI.getInstance().getGameOfPlayer(buyer).getTeamOfPlayer(buyer);

        if (!ShopUtil.addEnchantsToPlayerTools(buyer, stack, name, enchantment)) return false;

        team.getConnectedPlayers().forEach(player->{
            if(player == null) return;
            player.sendMessage("§c" + buyer.getName() + "§e has upgraded team sword damage!");
            if (player == buyer) return;
            ShopUtil.addEnchantsToPlayerTools(player, stack, name, enchantment);
        });

        return true;
    }

    static <K, V> List<K> getAllKeysForValue(Map<K, V> mapOfWords, V value) {
        List<K> listOfKeys = null;
        if (mapOfWords.containsValue(value)) {
            listOfKeys = new ArrayList<>();

            for (Map.Entry<K, V> entry : mapOfWords.entrySet()) {
                if (entry.getValue().equals(value)) {
                    listOfKeys.add(entry.getKey());
                }
            }
        }
        return listOfKeys;
    }

    public static List<Game> getGamesWithSize(int c) {
        final List<String> maps = getAllKeysForValue(Configurator.game_size, c);
        if (maps == null || maps.isEmpty())
            return null;

        final ArrayList<Game> listofgames = new ArrayList<>();

        maps.forEach(map->{
            if(Main.getGameNames().contains(map))
                listofgames.add(Main.getGame(map));
        });

        return listofgames;
    }

    public static FormatBuilder createBuilder(ArrayList<Object> games, ItemStack category, ItemStack category2, ItemStack category3,
                                              ItemStack category4) {
        final FormatBuilder builder = new FormatBuilder();
        final Map<String, Object> options = new HashMap<>();

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
                .set("row", 3)
                .set("column", 8);

        return builder;
    }


    public static <K, V> K getKey(Map<K, V> map, V value) {
        for (K key : map.keySet()) {
            if (value.equals(map.get(key))) {
                return key;
            }
        }
        return null;
    }


    public static void giveItemToPlayer(List<ItemStack> itemStackList, Player player, TeamColor teamColor) {
        if (itemStackList == null) return;

       itemStackList.forEach(itemStack -> {

           if(itemStack == null){
               return;
           }

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
        });

    }

    public static ItemStack checkifUpgraded(ItemStack newItem) {
        try {
            if (UpgradeKeys.get(newItem.getType().name().substring(0, newItem.getType().name().indexOf("_"))) > 1) {
                final Map<Enchantment, Integer> enchant = newItem.getEnchantments();
                final String typeName = newItem.getType().name();
                final int upgradeValue = UpgradeKeys.get(typeName.substring(0, typeName.indexOf("_"))) - 1;
                final Material mat = Material.valueOf(getKey(UpgradeKeys, upgradeValue) + typeName.substring(typeName.lastIndexOf("_")));
                ItemStack temp = new ItemStack(mat);
                temp.addEnchantments(enchant);
                return temp;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return newItem;
    }


    static public String capFirstLetter(String str) {
        String firstLetter = str.substring(0, 1).toUpperCase();
        String restLetters = str.substring(1).toLowerCase();
        return firstLetter + restLetters;
    }

    public static ArrayList<Object> createGamesGUI(int mode, List<String> lore) {
        if (Arrow == null)
            InitalizeStacks();

        final ArrayList<Object> games = new ArrayList<>();
        int items = 0;

        final ItemStack arenaMaterial = new ItemStack(Material
                .valueOf(SBAHypixelify.getConfigurator().config
                        .getString("games-inventory.stack-material", "PAPER")));

        final ItemMeta arenaMatMeta = arenaMaterial.getItemMeta();

        for (org.screamingsandals.bedwars.api.game.Game game : BedwarsAPI.getInstance()
                .getGames()) {
            if (Configurator.game_size.containsKey(game.getName()) &&
                    Configurator.game_size.get(game.getName()).equals(mode) && items < 28) {
                String name1 = "§a" + game.getName();
                List<String> newLore = new ArrayList<>();
                lore.forEach(ls->{
                    if(ls == null || ls.isEmpty()){
                        return;
                    }
                    newLore.add(
                            ls.replace("{players}", String.valueOf(game.getConnectedPlayers().size()))
                                    .replace("{status}", capFirstLetter(game.getStatus().name()))
                    );
                });
                arenaMatMeta.setLore(newLore);
                arenaMatMeta.setDisplayName(name1);
                arenaMaterial.setItemMeta(arenaMatMeta);
                HashMap<String, Object> gameStack = new HashMap<>();
                gameStack.put("stack", arenaMaterial);
                gameStack.put("game", game);
                games.add(gameStack);
                items++;
            }
        }

        ItemStack arrowStack = Arrow;
        HashMap<String, Object> arrows = new HashMap<>();
        arrows.put("stack", arrowStack);
        arrows.put("row", 5);
        arrows.put("column", 4);
        arrows.put("locate", "main");

        ItemStack fs = FireWorks;
        ItemMeta fsMeta = fs.getItemMeta();
        String size = getGamesWithSize(mode) == null ? "0" : String.valueOf(Objects.requireNonNull(getGamesWithSize(mode)).size());

        List<String> fsMetaLore = SBAHypixelify.getConfigurator().config.getStringList("games-inventory.fireworks-lore");
        List<String> tempList = new ArrayList<>();
        for (String st : fsMetaLore) {
            st = st
                    .replace("{mode}", getModeFromInt(mode))
                    .replace("{games}", size);
            tempList.add(st);
        }

        fsMeta.setLore(tempList);


        fs.setItemMeta(fsMeta);
        HashMap<String, Object> fireworks = new HashMap<>();
        fireworks.put("stack", fs);
        fireworks.put("row", 4);
        fireworks.put("column", 3);

        ItemStack Dia = Diamond;
        ItemMeta diaMeta = Dia.getItemMeta();
        diaMeta.setLore(fsMeta.getLore());
        Dia.setItemMeta(diaMeta);
        HashMap<String, Object> diamond = new HashMap<>();
        diamond.put("stack", Dia);
        diamond.put("row", 4);
        diamond.put("column", 5);

        games.add(arrows);
        games.add(fireworks);
        games.add(diamond);
        return games;
    }

    public static String getModeFromInt(int mode) {
        return mode == 1 ? "Solo" : mode == 2 ? "Double" : mode == 3 ? "Triples" : "Squads";
    }

    public static int getIntFromMode(String mode){
        return mode.equalsIgnoreCase("Solo") ? 1 :
                mode.equalsIgnoreCase("Double") ? 2 : mode.equalsIgnoreCase("Triples") ? 3 :
                mode.equalsIgnoreCase("Squads") ? 4 : 0;
    }

    public static Options generateOptions() {
        Options options = new Options(SBAHypixelify.getInstance());
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
                                                   String name, String name2) {
        List<ItemStack> myList = new ArrayList<>();

        ItemStack category;
        ItemStack category2;
        if (Main.isLegacy()) {
            category = new ItemStack(Material.valueOf("BED"));
            category2 = new ItemStack(Material.valueOf("SIGN"));
        } else {
            category = new ItemStack(Material.valueOf("RED_BED"));
            category2 = new ItemStack(Material.valueOf("OAK_SIGN"));
        }

        ItemStack category3 = new ItemStack(Material.BARRIER);
        ItemStack category4 = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = category.getItemMeta();
        meta.setLore(lore1);
        meta.setDisplayName(name);
        category.setItemMeta(meta);

        ItemMeta meta2 = category2.getItemMeta();
        meta2.setLore(SBAHypixelify.getConfigurator().config.getStringList("games-inventory.oak_sign-lore"));
        meta2.setDisplayName(name2);
        category2.setItemMeta(meta2);

        ItemMeta meta3 = category3.getItemMeta();
        String name3 = SBAHypixelify.getConfigurator().config.getString("games-inventory.barrier-name", "§cExit");
        meta3.setDisplayName(name3);
        category3.setItemMeta(meta3);

        ItemMeta meta4 = category4.getItemMeta();
        String name4 = SBAHypixelify.getConfigurator().config.getString("games-inventory.ender_pearl-name"
                , "§cClick here to rejoin!");

        meta4.setLore(SBAHypixelify.getConfigurator().config.getStringList("games-inventory.ender_pearl-lore"));
        meta4.setDisplayName(name4);
        category4.setItemMeta(meta4);

        myList.add(category);
        myList.add(category2);
        myList.add(category3);
        myList.add(category4);

        return myList;
    }

    public static String translateColors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }


    public static void sendMessage(Player player, List<String> message) {
        message.forEach(st -> player.sendMessage(translateColors(st)));
    }

    public static void upgradeSwordOnPurchase(Player player, ItemStack newItem, Game game) {
        if (SBAHypixelify.getConfigurator().config.getBoolean("remove-sword-on-upgrade", true)) {
            Arrays.stream(player.getInventory().getContents()).forEach(item -> {
                if (item == null) return;

                final String typeName = item.getType().name();

                if (typeName.endsWith("SWORD"))
                    player.getInventory().remove(item);

            });
        }
        int level;
        try {
            level = Objects.requireNonNull(SBAHypixelify.getGamestorage(game)).getSharpness(game.getTeamOfPlayer(player).getName());
        } catch (Throwable t) {
            return;
        }

        if (level != 0)
            newItem.addEnchantment(Enchantment.DAMAGE_ALL, level);
    }


    public static void removeAxeOrPickaxe(Player player, ItemStack newItem) {
        final String name = newItem.getType().name().substring(newItem.getType().name().indexOf("_"));

        for (ItemStack item : player.getInventory().getContents()) {
            if(item == null) return;

            final String typeName = item.getType().name();

            if(typeName.endsWith(name)){
                player.getInventory().remove(item);
            }
        }
    }

    public static String ChatColorChanger(Player player) {
        final PlayerWrapper db = SBAHypixelify.getWrapperService().getWrapper(player);
        if (db.getLevel() > 100 || player.isOp()) {
            return "§f";
        } else {
            return "§7";
        }
    }


}