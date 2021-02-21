package pronze.hypixelify.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.TeamColor;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStore;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.lib.sgui.builder.LocalOptionsBuilder;
import pronze.hypixelify.Configurator;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.listener.TeamUpgradeListener;

import java.util.*;

import static org.screamingsandals.bedwars.lib.lang.I.i18n;

public class ShopUtil {

    private final static Map<String, Integer> UpgradeKeys = new HashMap<>();

    public static void initKeys() {
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
    }

    public static void addEnchantsToPlayerArmor(Player player, ItemStack newItem) {
        Arrays.stream(player.getInventory().getArmorContents())
                .filter(Objects::nonNull)
                .forEach(item -> item.addEnchantments(newItem.getEnchantments()));
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
        final var newItemEnchantLevel = newItem.getEnchantmentLevel(enchantment);

        for (final var item : buyer.getInventory().getContents()) {
            if (item == null) continue;

            final var typeName = item.getType().name();
            final var itemEnchantLevel = item.getEnchantmentLevel(enchantment);
            if (typeName.endsWith(name)) {
                if (itemEnchantLevel >= newItemEnchantLevel || newItemEnchantLevel >= 5)
                    return false;

                item.addEnchantments(newItem.getEnchantments());
            }
        }

        return true;
    }

    public static boolean addEnchantsToTeamTools(Player buyer, ItemStack stack, String name, Enchantment enchantment) {
        final var team = BedwarsAPI.getInstance().getGameOfPlayer(buyer).getTeamOfPlayer(buyer);

        if (!ShopUtil.addEnchantsToPlayerTools(buyer, stack, name, enchantment)) return false;

        team.getConnectedPlayers()
                .stream()
                .filter(Objects::nonNull)
                .forEach(player -> {
                    player.sendMessage("§c" + buyer.getName() + "§e has upgraded team sword damage!");
                    if (player != buyer) {
                        ShopUtil.addEnchantsToPlayerTools(player, stack, name, enchantment);
                    }
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
        final var maps = getAllKeysForValue(Configurator.game_size, c);
        if (maps == null || maps.isEmpty())
            return null;

        final var gameList = new ArrayList<Game>();

        maps.stream()
                .filter(Main.getGameNames()::contains)
                .forEach(map -> gameList.add(Main.getGame(map)));

        return gameList;
    }

    public static <K, V> K getKey(Map<K, V> map, V value) {
        return map.keySet()
                .stream()
                .filter(key -> value.equals(map.get(key)))
                .findFirst()
                .orElse(null);
    }


    public static void giveItemToPlayer(List<ItemStack> itemStackList, Player player, TeamColor teamColor) {
        if (itemStackList == null) return;
        final var colorChanger = BedwarsAPI.getInstance().getColorChanger();

        itemStackList
                .stream()
                .filter(Objects::nonNull)
                .forEach(itemStack -> {
                    final var materialName = itemStack.getType().toString();
                    final var playerInventory = player.getInventory();

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
                final var enchant = newItem.getEnchantments();
                final var typeName = newItem.getType().name();
                final var upgradeValue = UpgradeKeys.get(typeName.substring(0, typeName.indexOf("_"))) - 1;
                final var mat = Material.valueOf(getKey(UpgradeKeys, upgradeValue) + typeName.substring(typeName.lastIndexOf("_")));
                final var temp = new ItemStack(mat);
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
                mode.equalsIgnoreCase("Double") ? 2 :
                        mode.equalsIgnoreCase("Triples") ? 3 :
                                mode.equalsIgnoreCase("Squads") ? 4 : 0;
    }

    public static void generateOptions(LocalOptionsBuilder localOptionsBuilder) {
        final var backItem = Main.getConfigurator().readDefinedItem("shopback", "BARRIER");
        backItem.setDisplayName(Component.text(i18n("shop_back", false)));
        localOptionsBuilder.backItem(backItem);

        final var pageBackItem = Main.getConfigurator().readDefinedItem("pageback", "ARROW");
        pageBackItem.setDisplayName(Component.text(i18n("page_back", false)));
        localOptionsBuilder.pageBackItem(pageBackItem);

        final var pageForwardItem = Main.getConfigurator().readDefinedItem("pageforward", "ARROW");
        pageForwardItem.setDisplayName(Component.text(i18n("page_forward", false)));
        localOptionsBuilder.pageForwardItem(pageForwardItem);

        final var cosmeticItem = Main.getConfigurator().readDefinedItem("shopcosmetic", "AIR");
        localOptionsBuilder
                .cosmeticItem(cosmeticItem)
                .renderHeaderStart(600)
                .renderFooterStart(600)
                .renderOffset(9)
                .rows(4)
                .renderActualRows(4)
                .showPageNumber(false);
    }

    public static String translateColors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }


    public static void sendMessage(Player player, List<String> message) {
        message.forEach(st -> player.sendMessage(translateColors(st)));
    }

    public static void upgradeSwordOnPurchase(Player player, ItemStack newItem, Game game) {
        if (SBAHypixelify.getConfigurator().config.getBoolean("remove-sword-on-upgrade", true)) {
            Arrays.stream(player.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .filter(stack -> stack.getType().name().endsWith("SWORD"))
                    .forEach(player.getInventory()::removeItem);
        }

        final var optionalGameStorage = SBAHypixelify.getStorage(game);

        if (optionalGameStorage.isEmpty()) {
            return;
        }

        int level = optionalGameStorage.get().getSharpness(game.getTeamOfPlayer(player).getName());
        if (level != 0)
            newItem.addEnchantment(Enchantment.DAMAGE_ALL, level);
    }


    public static void removeAxeOrPickaxe(Player player, ItemStack newItem) {
        final String name = newItem.getType().name().substring(newItem.getType().name().indexOf("_"));

        Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(stack -> stack.getType().name().endsWith(name))
                .forEach(player.getInventory()::remove);
    }

    public static String ChatColorChanger(Player player) {
        final var db = SBAHypixelify.getWrapperService().getWrapper(player);
        if (db.getLevel() > 100 || player.isOp()) {
            return "§f";
        } else {
            return "§7";
        }
    }

    public static Integer getSharpnessOrProtectionLevel(String property, ItemStack stack) {
        if (stack == null || stack.getEnchantments().isEmpty()) {
            return null;
        }

        final var enchant = property.equalsIgnoreCase("sharpness") ? Enchantment.DAMAGE_ALL :
                Enchantment.PROTECTION_ENVIRONMENTAL;

        if (!stack.getEnchantments().containsKey(enchant)) return null;

        final var level = stack.getEnchantmentLevel(enchant);
        if (level <= 0 || level >= 5) return null;
        return TeamUpgradeListener.prices.get(level);
    }


}