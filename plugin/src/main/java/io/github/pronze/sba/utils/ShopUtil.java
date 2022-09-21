package io.github.pronze.sba.utils;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.data.DegradableItem;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.game.IGameStorage;
import io.github.pronze.sba.game.StoreType;
import io.github.pronze.sba.inventories.SBAStoreInventoryV2;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.service.PlayerWrapperService;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.TeamColor;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.api.utils.ColorChanger;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.item.meta.EnchantmentMapping;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.simpleinventories.builder.LocalOptionsBuilder;
import org.screamingsandals.simpleinventories.events.ItemRenderEvent;
import org.screamingsandals.simpleinventories.inventory.PlayerItemInfo;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ShopUtil {

    public static final List<String> romanNumerals = List.of("NONE", "I", "II", "III", "IV", "V", "VI", "VII", "VIII",
            "IX", "X");
    public static final List<String> orderOfArmor = List.of("GOLDEN,GOLD", "CHAINMAIL", "IRON", "DIAMOND", "NETHERITE");
    public static final List<String> orderOfTools = List.of("WOODEN,WOOD", "STONE", "GOLDEN,GOLD", "IRON", "DIAMOND");

    @NotNull
    public static Integer getLevelFromMaterialName(@NotNull String name, final List<String> list) {
        name = name.substring(0, name.contains("_") ? name.lastIndexOf("_") : name.length());
        @NotNull
        String finalName = name;
        return list.stream()
                .filter(value -> Arrays.stream(value.split(",")).anyMatch(names -> names.equalsIgnoreCase(finalName)))
                .map(list::indexOf)
                .findAny()
                .orElse(0);
    }

    @NotNull
    public static String getMaterialFromLevel(int level, DegradableItem itemType) {
        final var list = itemType == DegradableItem.ARMOR ? orderOfArmor : orderOfTools;
        var obj = list.get(level);
        if (obj == null) {
            obj = list.get(0);
        }

        final var toTest = itemType == DegradableItem.ARMOR ? "_BOOTS" : "_AXE";
        var toParse = obj.split(",");
        for (String matName : toParse) {
            try {
                Material.valueOf(matName + toTest);
                return matName;
            } catch (IllegalArgumentException ignored) {
            }
        }

        return list.get(0);
    }

    @NotNull
    public static String getMaterialFromArmorOrTools(@NotNull String material) {
        return material.substring(0, material.indexOf("_")).toUpperCase();
    }

    @NotNull
    public static String getMaterialFromArmorOrTools(@NotNull Material material) {
        return getMaterialFromArmorOrTools(material.name());
    }

    public static boolean buyArmor(Player player, Material mat_boots, IGameStorage gameStorage, Game game) {
        final var playerInventory = player.getInventory();
        final var playerBoots = playerInventory.getBoots();
        final var matName = getMaterialFromArmorOrTools(mat_boots);

        if (playerBoots != null) {
            final var currentMat = playerBoots.getType();
            final var currentMatName = getMaterialFromArmorOrTools(currentMat);

            int currentLevel = getLevelFromMaterialName(currentMatName, orderOfArmor);
            int newLevel = getLevelFromMaterialName(matName, orderOfArmor);

            if (!SBAConfig.getInstance().node("can-downgrade-item").getBoolean(false)) {
                if (currentLevel > newLevel) {
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.CANNOT_DOWNGRADE_ITEM)
                            .replace("%item%", "armor")
                            .send(PlayerMapper.wrapPlayer(player));
                    return false;
                }
            }

            if (currentLevel == newLevel) {
                LanguageService
                        .getInstance()
                        .get(MessageKeys.ALREADY_PURCHASED)
                        .replace("%thing%", "armor")
                        .send(PlayerMapper.wrapPlayer(player));
                return false;
            }
        }

        final var boots = new ItemStack(mat_boots);
        final var leggings = new ItemStack(Material.valueOf(matName + "_LEGGINGS"));
        final var chestplate = new ItemStack(Material.valueOf(matName + "_CHESTPLATE"));
        final var helmet = new ItemStack(Material.valueOf(matName + "_HELMET"));

        applyTeamEnchants(player, boots);
        applyTeamEnchants(player, leggings);
        applyTeamEnchants(player, chestplate);
        applyTeamEnchants(player, helmet);

        playerInventory.setLeggings(null);
        playerInventory.setBoots(null);

        if (SBAConfig.getInstance().upgrades().boots())
            playerInventory.setBoots(boots);
        if (SBAConfig.getInstance().upgrades().leggings())
            playerInventory.setLeggings(leggings);
        if (SBAConfig.getInstance().upgrades().chestplate())
            playerInventory.setChestplate(chestplate);
        if (SBAConfig.getInstance().upgrades().helmet())
            playerInventory.setHelmet(helmet);
        return true;
    }

    public static void increaseTeamEnchant(Player teamPlayer, @Nullable ItemStack item, Enchantment damageAll) {
        if (!canApply(damageAll, item))
            return;
        int level = item.getEnchantmentLevel(damageAll);
        item.addUnsafeEnchantment(damageAll, level + 1);
    }

    public static ItemStack applyTeamEnchants(Player player, ItemStack newItem) {
        final var game = Main.getInstance().getGameOfPlayer(player);
        var gameStorage = SBA
                .getInstance()
                .getGameStorage(game)
                .orElseThrow();

        final var typeName = newItem.getType().name();
        final var team = game.getTeamOfPlayer(player);

        int sharpnessLevel = gameStorage.getSharpnessLevel(team).orElse(0);
        if (sharpnessLevel > 0 && canApply(Enchantment.DAMAGE_ALL, newItem))
            newItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpnessLevel);
        int knockbackLebel = gameStorage.getKnockbackLevel(team).orElse(0);
        if (knockbackLebel > 0 && canApply(Enchantment.KNOCKBACK, newItem))
            newItem.addUnsafeEnchantment(Enchantment.KNOCKBACK, knockbackLebel);
        int efficiencyLevel = gameStorage.getEfficiencyLevel(team).orElse(0);
        if (efficiencyLevel > 0 && canApply(Enchantment.DIG_SPEED, newItem))
            newItem.addUnsafeEnchantment(Enchantment.DIG_SPEED, efficiencyLevel);
        int protectionLevel = gameStorage.getProtectionLevel(team).orElse(0);
        if (protectionLevel > 0 && canApply(Enchantment.PROTECTION_ENVIRONMENTAL, newItem))
            newItem.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protectionLevel);
        List<String> ignoredKeys = List.of("sharpness", "knockback", "protection", "efficiency");
        SBAConfig.getInstance().upgrades().enchants().keys().forEach(ench -> {
            Optional<Enchantment> ec = Arrays.stream(Enchantment.values())
                    .filter(x -> x.getName().equalsIgnoreCase(ench) || x.getKey().asString().equalsIgnoreCase(ench))
                    .findFirst();
            if (ignoredKeys.contains(ench))
                return;
            if (!canApply(ench, newItem))
                return;
            if (!ec.isPresent()) {
                Logger.error(
                        "SBA doesn't know how to apply enchant {}, it is not a valid enchant, check https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html for a list of enchant on your version of minecraft",
                        ench);
                return;
            }
            Enchantment ech = ec.get();
            int level = gameStorage.getEnchantLevel(team, ench).orElse(0);
            if (level > 0)
                newItem.addUnsafeEnchantment(ech, level);
        });
        return newItem;
    }

    private static boolean canApply(String string, ItemStack newItem) {
        if (SBAConfig.getInstance().upgrades().enchants().of(string) == null) {
            Logger.error("SBA doesn't know how to apply enchant {}, add it in the upgrade-item.enchants.ENCHANT_HERE",
                    string);
            return false;
        }
        return SBAConfig.getInstance().upgrades().enchants().of(string).stream()
                .anyMatch(x -> newItem.getType().toString().contains(x.toUpperCase()));
    }

    private static boolean canApply(Enchantment string, ItemStack newItem) {
        return canApply(getName(string), newItem);
    }

    private static String getName(Enchantment ech) {
        if (ech == Enchantment.DAMAGE_ALL)
            return ("sharpness");
        if (ech == Enchantment.KNOCKBACK)
            return ("sharpness");
        if (ech == Enchantment.DIG_SPEED)
            return ("efficiency");
        if (ech == Enchantment.PROTECTION_ENVIRONMENTAL)
            return ("protection");
        AtomicReference<String> str = new AtomicReference<>();
        SBAConfig.getInstance().upgrades().enchants().keys().forEach(ench -> {
            Optional<Enchantment> ec = Arrays.stream(Enchantment.values())
                    .filter(x -> x.getName().equalsIgnoreCase(ench) || x.getKey().asString().equalsIgnoreCase(ench))
                    .findFirst();
            if(ec.isPresent() && ec.get().equals(ech))
                str.set(ench);
        });
        return str.get();

    }

    static <K, V> List<K> getAllKeysForValue(Map<K, V> mapOfWords, V value) {
        return mapOfWords.entrySet()
                .stream()
                .filter((entry) -> entry.getValue().equals(value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static <K, V> K getKey(Map<K, V> map, V value) {
        return map.keySet()
                .stream()
                .filter(key -> value.equals(map.get(key)))
                .findAny()
                .orElse(null);
    }

    public static void giveItemToPlayer(@NotNull List<ItemStack> itemStackList, Player player, TeamColor teamColor) {
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

    public static ItemStack downgradeItem(ItemStack currentItem, DegradableItem itemType) {
        final var currentItemName = getMaterialFromArmorOrTools(currentItem.getType());
        final int currentItemLevel = getLevelFromMaterialName(currentItemName, orderOfTools);

        if (currentItemLevel == 0) {
            return currentItem;
        }

        final var newItemLevel = currentItemLevel - 1;
        final var newMaterialName = getMaterialFromLevel(newItemLevel, itemType);
        final var newMaterial = Material.valueOf(newMaterialName
                + currentItem.getType().name().substring(currentItem.getType().name().lastIndexOf("_")).toUpperCase());
        final var newStack = new ItemStack(newMaterial);
        newStack.addEnchantments(currentItem.getEnchantments());
        return newStack;
    }

    public static int getIntFromMode(String mode) {
        return mode.equalsIgnoreCase("Solo") ? 1
                : mode.equalsIgnoreCase("Double") ? 2
                        : mode.equalsIgnoreCase("Triples") ? 3 : mode.equalsIgnoreCase("Squads") ? 4 : 0;
    }

    public static String translateColors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void sendMessage(Player player, List<String> message) {
        message.forEach(st -> player.sendMessage(translateColors(st)));
    }

    public static File normalizeShopFile(String name) {
        var dataFolder = SBA.getBedwarsPlugin().getDataFolder();
        if (name.split("\\.").length > 1) {
            return dataFolder.toPath().resolve(name).toFile();
        }

        var fileg = dataFolder.toPath().resolve(name + ".groovy").toFile();
        if (fileg.exists()) {
            return fileg;
        }
        return dataFolder.toPath().resolve(name + ".yml").toFile();
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

    public static Item setLore(Item item, PlayerItemInfo itemInfo, String price, ItemSpawnerType type, Player player) {
        var enabled = itemInfo.getFirstPropertyByName("generateLore")
                .map(property -> property.getPropertyData().getBoolean())
                .orElseGet(() -> Main.getConfigurator().config.getBoolean("lore.generate-automatically", true));

        if (enabled) {
            final var originalList = item.getLore();

            final var isSharp = itemInfo.getFirstPropertyByName("sharpness").isPresent();
            final var isProt = itemInfo.getFirstPropertyByName("protection").isPresent();
            final var isEfficiency = itemInfo.getFirstPropertyByName("efficiency").isPresent();

            final var game = Main.getInstance().getGameOfPlayer(player);
            final var arena = ArenaManager
                    .getInstance()
                    .get(game.getName())
                    .orElseThrow();

            if (isSharp) {
                final var currentLevel = arena.getStorage().getSharpnessLevel(game.getTeamOfPlayer(player))
                        .orElseThrow() + 1;
                final var limit = SBAConfig.getInstance().node("upgrades", "limit", "Sharpness").getInt(2);
                if (currentLevel <= limit) {
                    price = String.valueOf(SBAStoreInventoryV2.sharpnessPrices
                            .get(arena.getStorage().getSharpnessLevel(game.getTeamOfPlayer(player)).orElseThrow() + 1));
                }
            }

            if (isProt) {
                final var currentLevel = arena.getStorage().getProtectionLevel(game.getTeamOfPlayer(player))
                        .orElseThrow() + 1;
                final var limit = SBAConfig.getInstance().node("upgrades", "limit", "Protection").getInt(4);
                if (currentLevel <= limit) {
                    price = String.valueOf(SBAStoreInventoryV2.protectionPrices.get(
                            arena.getStorage().getProtectionLevel(game.getTeamOfPlayer(player)).orElseThrow() + 1));
                }
            }

            if (isEfficiency) {
                final var currentLevel = arena.getStorage().getEfficiencyLevel(game.getTeamOfPlayer(player))
                        .orElseThrow() + 1;
                final var limit = SBAConfig.getInstance().node("upgrades", "limit", "Efficiency").getInt(4);
                if (currentLevel <= limit) {
                    price = String.valueOf(SBAStoreInventoryV2.efficiencyPrices.get(
                            arena.getStorage().getEfficiencyLevel(game.getTeamOfPlayer(player)).orElseThrow() + 1));
                }
            }

            String finalPrice = price;
            final var newList = itemInfo.getFirstPropertyByName("generatedLoreText")
                    .map(property -> property.getPropertyData().childrenList().stream()
                            .map(ConfigurationNode::getString))
                    .orElseGet(() -> Main.getConfigurator().config.getStringList("lore.text").stream())
                    .map(s -> s
                            .replaceAll("%price%", finalPrice)
                            .replaceAll("%resource%", type.getItemName())
                            .replaceAll("%amount%", Integer.toString(itemInfo.getStack().getAmount())))
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .map(AdventureHelper::toComponent)
                    .collect(Collectors.toCollection((Supplier<ArrayList<Component>>) ArrayList::new));
            newList.addAll(originalList);

            return item.withItemLore(newList);
        }
        return item;
    }

    public static String getNameOrCustomNameOfItem(Item item) {
        try {
            if (item.getDisplayName() != null) {
                return AdventureHelper.toLegacy(item.getDisplayName());
            }
            /*
             * if (item.getLocalizedName() != null) {
             * return AdventureHelper.toLegacy(item.getLocalizedName());
             * }
             */
        } catch (Throwable ignored) {
        }

        var normalItemName = item.getMaterial().platformName().replace("_", " ").toLowerCase();
        var sArray = normalItemName.split(" ");
        var stringBuilder = new StringBuilder();

        for (var s : sArray) {
            stringBuilder.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    public static void addEnchantsToPlayerArmor(Player player, int newLevel) {
        for (var item : player.getInventory().getArmorContents()) {
            if (item != null) {
                applyTeamEnchants(player, item);
            }
        }
    }

    public static Item clampOrApplyEnchants(Item item, int level, Enchantment enchantment, StoreType type,
            int maxLevel) {
        if (type == StoreType.UPGRADES) {
            level = level + 1;
        }
        if (level > maxLevel) {

            item = item.withItemLore(LanguageService
                    .getInstance()
                    .get(MessageKeys.SHOP_MAX_ENCHANT)
                    .toComponentList());

            if (item.getEnchantments() != null) {
                item.getEnchantments().clear();
            }
        } else if (level > 0) {
            item = item.withEnchantment(EnchantmentMapping.resolve(enchantment).orElseThrow().withLevel(level));
        }
        return item;
    }

    /**
     * Applies enchants to displayed items in SBA store inventory.
     * Enchants are applied and are dependent on the team upgrades the player's team
     * has.
     *
     * @param item
     * @param event
     */
    public static Item applyTeamUpgradeEnchantsToItem(Item item, ItemRenderEvent event, StoreType type) {
        final var player = event.getPlayer().as(Player.class);
        final var game = Main.getInstance().getGameOfPlayer(player);
        final var typeName = item.getMaterial().platformName();
        final var runningTeam = game.getTeamOfPlayer(player);

        var prices = event.getInfo().getOriginal().getPrices();
        if (!prices.isEmpty()) {

            ArrayList<Component> lore = new ArrayList<>(item.getLore());

            lore.add(
                    (LanguageService
                            .getInstance()
                            .get(MessageKeys.CLICK_TO_PURCHASE)
                            .toComponent()));
            item = item.withItemLore(lore);
        }

        var maybeStorage = SBA.getInstance()
                .getGameStorage(game);
        if (maybeStorage.isPresent()) {
            var gameStorage = maybeStorage.get();
            final var afterUnderscore = typeName
                    .substring(typeName.contains("_") ? typeName.indexOf("_") + 1 : 0);
            switch (afterUnderscore.toLowerCase()) {
                case "sword":
                    int sharpness = gameStorage.getSharpnessLevel(runningTeam).orElseThrow();
                    item = clampOrApplyEnchants(item, sharpness, Enchantment.DAMAGE_ALL, type,
                            SBAConfig.getInstance().node("upgrades", "limit", "Sharpness").getInt(1));
                    break;
                case "chestplate":
                case "boots":
                    int protection = gameStorage.getProtectionLevel(runningTeam).orElseThrow();
                    item = clampOrApplyEnchants(item, protection, Enchantment.PROTECTION_ENVIRONMENTAL, type,
                            SBAConfig.getInstance().node("upgrades", "limit", "Protection").getInt(4));
                    break;
                case "pickaxe":
                    final int efficiency = gameStorage.getEfficiencyLevel(runningTeam).orElseThrow();
                    item = clampOrApplyEnchants(item, efficiency, Enchantment.DIG_SPEED, type,
                            SBAConfig.getInstance().node("upgrades", "limit", "Efficiency").getInt(2));
                    break;
            }

        }
        return item;
    }

    // TODO:
    public static void generateOptions(LocalOptionsBuilder localOptionsBuilder) {
        final var backItem = Main.getConfigurator().readDefinedItem("shopback", "BARRIER");
        final var backItemMeta = backItem.getItemMeta();

        // backItemMeta.setDisplayName(Message.of());
        //
        // backItem.setDisplayName(Message.of(LangKeys.IN_GAME_SHOP_SHOP_BACK).asComponent());
        // localOptionsBuilder.backItem(backItem);

        // final var pageBackItem = MainConfig.getInstance().readDefinedItem("pageback",
        // "ARROW");
        // pageBackItem.setDisplayName(Message.of(LangKeys.IN_GAME_SHOP_PAGE_BACK).asComponent());
        // localOptionsBuilder.pageBackItem(pageBackItem);

        // final var pageForwardItem =
        // MainConfig.getInstance().readDefinedItem("pageforward", "ARROW");
        // pageForwardItem.setDisplayName(Message.of(LangKeys.IN_GAME_SHOP_PAGE_FORWARD).asComponent());
        // localOptionsBuilder.pageForwardItem(pageForwardItem);

        // final var cosmeticItem =
        // MainConfig.getInstance().readDefinedItem("shopcosmetic", "AIR");
        localOptionsBuilder
                // .cosmeticItem(cosmeticItem)
                .renderHeaderStart(600)
                .renderFooterStart(600)
                .renderOffset(9)
                .rows(4)
                .renderActualRows(4)
                .showPageNumber(false);
    }

    public static String ChatColorChanger(Player player) {
        final SBAPlayerWrapper db = PlayerWrapperService.getInstance().get(player).orElseThrow();
        if (db.getLevel() > 100 || player.isOp()) {
            return "§f";
        } else {
            return "§7";
        }
    }

    public static void applyTeamUpgrades(@NotNull Player player, Game game) {
        final var team = game.getTeamOfPlayer(player);
        final var gameStorage = ArenaManager
                .getInstance()
                .get(game.getName())
                .orElseThrow()
                .getStorage();
        final var teamProtectionLevel = gameStorage.getProtectionLevel(team).orElse(0);
        if (teamProtectionLevel > 0)
            ShopUtil.addEnchantsToPlayerArmor(player, teamProtectionLevel);
        final var finalTeamSharpnessLevel = gameStorage.getSharpnessLevel(team).orElse(0);
        final var finalTeamEfficiencyLevel = gameStorage.getEfficiencyLevel(team).orElse(0);

        Logger.trace("Player teamProtectionLevel {}", teamProtectionLevel);
        Logger.trace("Player finalTeamSharpnessLevel {}", finalTeamSharpnessLevel);
        Logger.trace("Player finalTeamEfficiencyLevel {}", finalTeamEfficiencyLevel);

        Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .forEach(item -> {
                    applyTeamEnchants(player, item);
                });
    }

}