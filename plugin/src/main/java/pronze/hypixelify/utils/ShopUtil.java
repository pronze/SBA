package pronze.hypixelify.utils;

import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.lib.material.Item;
import org.screamingsandals.lib.material.meta.EnchantmentMapping;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.simpleinventories.builder.LocalOptionsBuilder;
import org.screamingsandals.simpleinventories.events.ItemRenderEvent;
import org.screamingsandals.simpleinventories.inventory.PlayerItemInfo;
import org.spongepowered.configurate.ConfigurationNode;
import pronze.hypixelify.SBAHypixelify;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.api.game.StoreType;
import pronze.hypixelify.api.lang.Message;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.TeamColor;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.utils.ColorChanger;
import org.screamingsandals.bedwars.lib.sgui.builder.FormatBuilder;
import org.screamingsandals.bedwars.lib.sgui.inventory.Options;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.game.ArenaManager;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.service.PlayerWrapperService;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.screamingsandals.bedwars.lib.lang.I.i18n;

public class ShopUtil {

    private final static Map<String, Integer> UpgradeKeys = new HashMap<>() {
        {
            put("STONE", 2);
            put("IRON", 4);
            put("DIAMOND", 5);
            if (Main.isLegacy()) {
                put("WOOD", 1);
                put("GOLD", 3);
            } else {
                put("WOODEN", 1);
                put("GOLDEN", 3);
            }
        }
    };

    public static void addEnchantsToPlayerArmor(Player player, ItemStack newItem) {
        Arrays.stream(player.getInventory().getArmorContents()).forEach(item ->{
            if(item != null){
                item.addEnchantments(newItem.getEnchantments());
            }
        });
    }

    public static void buyArmor(Player player, Material mat_boots, GameStorage gameStorage, Game game) {
        final var matName = mat_boots.name().substring(0, mat_boots.name().indexOf("_"));
        final var mat_leggings = Material.valueOf(matName + "_LEGGINGS");
        final var boots = new ItemStack(mat_boots);
        final var leggings = new ItemStack(mat_leggings);

        final var level = gameStorage.getProtection(game.getTeamOfPlayer(player).getName());
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
        final List<String> maps = getAllKeysForValue(SBAConfig.game_size, c);
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

    public static String getModeFromInt(int mode) {
        return mode == 1 ? "Solo" : mode == 2 ? "Double" : mode == 3 ? "Triples" : "Squads";
    }

    public static int getIntFromMode(String mode){
        return mode.equalsIgnoreCase("Solo") ? 1 :
                mode.equalsIgnoreCase("Double") ? 2 : mode.equalsIgnoreCase("Triples") ? 3 :
                mode.equalsIgnoreCase("Squads") ? 4 : 0;
    }

    public static String translateColors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }


    public static void sendMessage(Player player, List<String> message) {
        message.forEach(st -> player.sendMessage(translateColors(st)));
    }

    public static void upgradeSwordOnPurchase(Player player, ItemStack newItem, Game game) {
        if (SBAHypixelify.getInstance().getConfigurator().getBoolean("remove-sword-on-upgrade", true)) {
            Arrays.stream(player.getInventory().getContents()).forEach(item -> {
                if (item == null) return;

                final String typeName = item.getType().name();

                if (typeName.endsWith("SWORD"))
                    player.getInventory().remove(item);

            });
        }
        int level;
        try {
            level = ArenaManager.getInstance().getGameStorage(game.getName()).orElseThrow().getSharpness(game.getTeamOfPlayer(player).getName());
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

    public static File normalizeShopFile(String name) {
        if (name.split("\\.").length > 1) {
            return SBAHypixelify.getPluginInstance().getDataFolder().toPath().resolve(name).toFile();
        }

        var fileg = SBAHypixelify.getPluginInstance().getDataFolder().toPath().resolve(name + ".groovy").toFile();
        if (fileg.exists()) {
            return fileg;
        }
        return SBAHypixelify.getPluginInstance().getDataFolder().toPath().resolve(name + ".yml").toFile();
    }

    public static Map<?, ?> nullValuesAllowingMap(Object... objects) {
        var map = new HashMap<>();
        Object key = null;
        for (var object : objects) {
            if (key == null) {
                key = Objects.requireNonNull(object);
            } else {
                map.put(key, object);
                key = null;
            }
        }
        return map;
    }

    public static void setLore(Item item, PlayerItemInfo itemInfo, String price, ItemSpawnerType type) {
        var enabled = itemInfo.getFirstPropertyByName("generateLore")
                .map(property -> property.getPropertyData().getBoolean())
                .orElseGet(() -> Main.getConfigurator().config.getBoolean("lore.generate-automatically", true));

        if (enabled) {
            var loreText = itemInfo.getFirstPropertyByName("generatedLoreText")
                    .map(property -> property.getPropertyData().childrenList().stream().map(ConfigurationNode::getString))
                    .orElseGet(() -> Main.getConfigurator().config.getStringList("lore.text").stream())
                    .map(s -> s
                            .replaceAll("%price%", price)
                            .replaceAll("%resource%", type.getItemName())
                            .replaceAll("%amount%", Integer.toString(itemInfo.getStack().getAmount())))
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .map(AdventureHelper::toComponent)
                    .collect(Collectors.toList());

            item.getLore().addAll(loreText);
        }
    }

    public static String getNameOrCustomNameOfItem(Item item) {
        try {
            if (item.getDisplayName() != null) {
                return AdventureHelper.toLegacy(item.getDisplayName());
            }
            if (item.getLocalizedName() != null) {
                return AdventureHelper.toLegacy(item.getLocalizedName());
            }
        } catch (Throwable ignored) {
        }

        var normalItemName = item.getMaterial().getPlatformName().replace("_", " ").toLowerCase();
        var sArray = normalItemName.split(" ");
        var stringBuilder = new StringBuilder();

        for (var s : sArray) {
            stringBuilder.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    public static void clampOrApplyEnchants(Item item, int level, Enchantment enchantment, StoreType type) {
        if (type == StoreType.UPGRADES) {
            level = level + 1;
        }
        if (level >= 5) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.SHOP_MAX_ENCHANT)
                    .toComponentList()
                    .forEach(item::addLore);
            if (item.getEnchantments() != null) {
                item.getEnchantments().clear();
            }
        } else if (level > 0){
            item.addEnchant(EnchantmentMapping.resolve(enchantment).orElseThrow().newLevel(level));
        }
    }


    /**
     * Applies enchants to displayed items in SBAHypixelify store inventory.
     * Enchants are applied and are dependent on the team upgrades the player's team has.
     *
     * @param item
     * @param event
     */
    public static void applyTeamUpgradeEnchantsToItem(Item item, ItemRenderEvent event, StoreType type) {
        final var player = event.getPlayer().as(Player.class);
        final var game = Main.getInstance().getGameOfPlayer(player);
        final var typeName = item.getMaterial().getPlatformName();
        final var runningTeam = game.getTeamOfPlayer(player);

        SBAHypixelify
                .getInstance()
                .getGameStorage(game)
                .ifPresent(gameStorage -> {
                    final var afterUnderscore = typeName.substring(typeName.contains("_") ? typeName.indexOf("_") + 1 : 0);
                    switch (afterUnderscore.toLowerCase()) {
                        case "sword":
                            int sharpness = gameStorage.getSharpness(runningTeam.getName());
                            clampOrApplyEnchants(item, sharpness, Enchantment.DAMAGE_ALL, type);
                            break;
                        case "chestplate":
                        case "boots":
                            int protection = gameStorage.getProtection(runningTeam.getName());
                            clampOrApplyEnchants(item, protection, Enchantment.PROTECTION_ENVIRONMENTAL, type);
                            break;
                        case "pickaxe":
                            final int efficiency = gameStorage.getEfficiency(runningTeam.getName());
                            clampOrApplyEnchants(item, efficiency, Enchantment.DIG_SPEED, type);
                            break;
                    }
                });
    }

    public static void generateOptions(LocalOptionsBuilder localOptionsBuilder) {
        final var backItem = Main.getConfigurator().readDefinedItem("shopback", "BARRIER");
   //backItem.setDisplayName(Message.of(LangKeys.IN_GAME_SHOP_SHOP_BACK).asComponent());
   //localOptionsBuilder.backItem(backItem);

   //final var pageBackItem = MainConfig.getInstance().readDefinedItem("pageback", "ARROW");
   //pageBackItem.setDisplayName(Message.of(LangKeys.IN_GAME_SHOP_PAGE_BACK).asComponent());
   //localOptionsBuilder.pageBackItem(pageBackItem);

   //final var pageForwardItem = MainConfig.getInstance().readDefinedItem("pageforward", "ARROW");
   //pageForwardItem.setDisplayName(Message.of(LangKeys.IN_GAME_SHOP_PAGE_FORWARD).asComponent());
   //localOptionsBuilder.pageForwardItem(pageForwardItem);

   //final var cosmeticItem = MainConfig.getInstance().readDefinedItem("shopcosmetic", "AIR");
        localOptionsBuilder
              //  .cosmeticItem(cosmeticItem)
                .renderHeaderStart(600)
                .renderFooterStart(600)
                .renderOffset(9)
                .rows(4)
                .renderActualRows(4)
                .showPageNumber(false);
    }

    public static String ChatColorChanger(Player player) {
        final PlayerWrapper db = PlayerWrapperService.getInstance().get(player).orElseThrow();
        if (db.getLevel() > 100 || player.isOp()) {
            return "§f";
        } else {
            return "§7";
        }
    }


}