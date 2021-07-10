package io.github.pronze.sba.utils;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.IGameStorage;
import io.github.pronze.sba.game.StoreType;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.service.PlayerWrapperService;
import io.github.pronze.sba.wrapper.PlayerWrapper;
import net.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.lib.material.Item;
import org.screamingsandals.lib.material.meta.EnchantmentMapping;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.simpleinventories.builder.LocalOptionsBuilder;
import org.screamingsandals.simpleinventories.events.ItemRenderEvent;
import org.screamingsandals.simpleinventories.inventory.PlayerItemInfo;
import org.spongepowered.configurate.ConfigurationNode;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.TeamColor;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.utils.ColorChanger;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.screamingsandals.bedwars.lib.lang.I.i18n;

public class ShopUtil {
    public static final Map<Integer, String> romanNumerals = new HashMap<>() {
        {
            put(1, "I");
            put(2, "II");
            put(3, "III");
            put(4, "IV");
            put(5, "V");
            put(6, "VI");
            put(7, "VII");
            put(8, "VIII");
            put(9, "IX");
            put(0, "X");
        }
    };

    private final static Map<String, Integer> UpgradeKeys = new HashMap<>() {
        {
            put("STONE", 2);
            put("CHAINMAIL", 4);
            put("IRON", 5);
            put("DIAMOND", 6);
            put("NETHERITE", 7);
            if (Main.isLegacy()) {
                put("WOOD", 1);
                put("GOLD", 3);
            } else {
                put("WOODEN", 1);
                put("GOLDEN", 3);
            }
        }
    };

    public static boolean buyArmor(Player player, Material mat_boots, IGameStorage gameStorage, Game game) {
        final var matName = mat_boots.name().substring(0, mat_boots.name().indexOf("_"));

        if (UpgradeKeys.containsKey(matName)) {
            final var playerBoots = player.getInventory().getBoots();
            if (playerBoots != null) {
                var keyLevel = UpgradeKeys.get(matName);
                var currentLevel = UpgradeKeys.get(playerBoots.getType().name().substring(0, playerBoots.getType().name().indexOf("_")));
                if (currentLevel == null) {
                    currentLevel = 0;
                }
                if (currentLevel > keyLevel) {
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.CANNOT_DOWNGRADE_ITEM)
                            .replace("<item>", "armor")
                            .send(PlayerMapper.wrapPlayer(player));
                    return false;
                }

                if (currentLevel.equals(keyLevel)) {
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.ALREADY_PURCHASED)
                            .replace("%thing%", "armor")
                            .send(PlayerMapper.wrapPlayer(player));
                    return false;
                }
            }
        }

        final var mat_leggings = Material.valueOf(matName + "_LEGGINGS");
        final var boots = new ItemStack(mat_boots);
        final var leggings = new ItemStack(mat_leggings);

        final var level = gameStorage.getProtectionLevel(game.getTeamOfPlayer(player)).orElseThrow();
        if (level != 0) {
            boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
            leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
        }
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        player.getInventory().setBoots(boots);
        player.getInventory().setLeggings(leggings);
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

    public static List<Game> getGamesWithSize(int size) {
        final List<String> maps = getAllKeysForValue(SBAConfig.game_size, size);
        if (maps == null || maps.isEmpty())
            return null;

        final ArrayList<Game> gameList = new ArrayList<>();

        maps.forEach(map -> {
            if (Main.getGameNames().contains(map))
                gameList.add(Main.getGame(map));
        });

        return gameList;
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

            if (itemStack == null) {
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


    public static int getIntFromMode(String mode) {
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

    public static File normalizeShopFile(String name) {
        if (name.split("\\.").length > 1) {
            return SBA.getPluginInstance().getDataFolder().toPath().resolve(name).toFile();
        }

        var fileg = SBA.getPluginInstance().getDataFolder().toPath().resolve(name + ".groovy").toFile();
        if (fileg.exists()) {
            return fileg;
        }
        return SBA.getPluginInstance().getDataFolder().toPath().resolve(name + ".yml").toFile();
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
            final var originalList = item.getLore();

            final var newList = itemInfo.getFirstPropertyByName("generatedLoreText")
                    .map(property -> property.getPropertyData().childrenList().stream().map(ConfigurationNode::getString))
                    .orElseGet(() -> Main.getConfigurator().config.getStringList("lore.text").stream())
                    .map(s -> s
                            .replaceAll("%price%", price)
                            .replaceAll("%resource%", type.getItemName())
                            .replaceAll("%amount%", Integer.toString(itemInfo.getStack().getAmount())))
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .map(AdventureHelper::toComponent).collect(Collectors.toCollection((Supplier<ArrayList<Component>>) ArrayList::new));
            newList.addAll(originalList);

            item.getLore().clear();
            item.getLore().addAll(newList);;
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


    public static void addEnchantsToPlayerArmor(Player player, int newLevel) {
        Arrays.stream(player.getInventory()
                .getArmorContents())
                .filter(Objects::nonNull)
                .forEach(item -> item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, newLevel));
    }

    public static void clampOrApplyEnchants(Item item, int level, Enchantment enchantment, StoreType type, int maxLevel) {
        if (type == StoreType.UPGRADES) {
            level = level + 1;
        }
        if (level > maxLevel) {
            item.getLore().clear();
            LanguageService
                    .getInstance()
                    .get(MessageKeys.SHOP_MAX_ENCHANT)
                    .toComponentList()
                    .forEach(item::addLore);
            if (item.getEnchantments() != null) {
                item.getEnchantments().clear();
            }
        } else if (level > 0) {
            item.addEnchant(EnchantmentMapping.resolve(enchantment).orElseThrow().newLevel(level));
        }
    }


    /**
     * Applies enchants to displayed items in SBA store inventory.
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

        var prices = event.getInfo().getOriginal().getPrices();
        if (!prices.isEmpty()) {
            item.addLore(LanguageService
                    .getInstance()
                    .get(MessageKeys.CLICK_TO_PURCHASE)
                    .toComponent());
        }

        SBA.getInstance()
                .getGameStorage(game)
                .ifPresent(gameStorage -> {
                    final var afterUnderscore = typeName.substring(typeName.contains("_") ? typeName.indexOf("_") + 1 : 0);
                    switch (afterUnderscore.toLowerCase()) {
                        case "sword":
                            int sharpness = gameStorage.getSharpnessLevel(runningTeam).orElseThrow();
                            clampOrApplyEnchants(item, sharpness, Enchantment.DAMAGE_ALL, type, SBAConfig.getInstance().node("upgrades", "limit", "Sharpness").getInt(1));
                            break;
                        case "chestplate":
                        case "boots":
                            int protection = gameStorage.getProtectionLevel(runningTeam).orElseThrow();
                            clampOrApplyEnchants(item, protection, Enchantment.PROTECTION_ENVIRONMENTAL, type, SBAConfig.getInstance().node("upgrades", "limit", "Protection").getInt(4));
                            break;
                        case "pickaxe":
                            final int efficiency = gameStorage.getEfficiencyLevel(runningTeam).orElseThrow();
                            clampOrApplyEnchants(item, efficiency, Enchantment.DIG_SPEED, type, SBAConfig.getInstance().node("upgrades", "limit", "Efficiency").getInt(2));
                            break;
                    }
                });
    }

    //TODO:
    public static void generateOptions(LocalOptionsBuilder localOptionsBuilder) {
        final var backItem = Main.getConfigurator().readDefinedItem("shopback", "BARRIER");
        final var backItemMeta = backItem.getItemMeta();
        //   backItemMeta.setDisplayName(Message.of());
//
        // backItem.setDisplayName(Message.of(LangKeys.IN_GAME_SHOP_SHOP_BACK).asComponent());
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