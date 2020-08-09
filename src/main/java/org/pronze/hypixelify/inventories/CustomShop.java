package org.pronze.hypixelify.inventories;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.api.events.PlayerToolUpgradeEvent;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent.Result;
import org.screamingsandals.bedwars.api.game.GameStore;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.api.upgrades.Upgrade;
import org.screamingsandals.bedwars.api.upgrades.UpgradeRegistry;
import org.screamingsandals.bedwars.api.upgrades.UpgradeStorage;
import org.screamingsandals.bedwars.game.CurrentTeam;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.game.GamePlayer;
import org.screamingsandals.bedwars.lib.sgui.SimpleInventories;
import org.screamingsandals.bedwars.lib.sgui.events.GenerateItemEvent;
import org.screamingsandals.bedwars.lib.sgui.events.PreActionEvent;
import org.screamingsandals.bedwars.lib.sgui.events.ShopTransactionEvent;
import org.screamingsandals.bedwars.lib.sgui.inventory.Options;
import org.screamingsandals.bedwars.lib.sgui.item.ItemProperty;
import org.screamingsandals.bedwars.lib.sgui.item.PlayerItemInfo;
import org.screamingsandals.bedwars.lib.sgui.utils.MapReader;
import org.screamingsandals.bedwars.utils.Debugger;
import org.screamingsandals.bedwars.utils.Sounds;
import sun.reflect.annotation.ExceptionProxy;


import java.io.File;
import java.util.*;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class CustomShop implements Listener {

    static public HashMap<Integer, Integer> Prices = new HashMap<>();
    private final Map<String, SimpleInventories> shopMap = new HashMap<>();
    private final Options options = new Options(Main.getInstance());

    public CustomShop() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Hypixelify.getInstance());

        Prices.put(0, Hypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-I", 4));
        Prices.put(1, Hypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-I", 4));
        Prices.put(2, Hypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-II", 8));
        Prices.put(3, Hypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-III", 12));
        Prices.put(4, Hypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-IV", 16));

        ItemStack backItem = Main.getConfigurator().readDefinedItem("shopback", "BARRIER");
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("back");
        backItem.setItemMeta(backItemMeta);
        options.setBackItem(backItem);

        ItemStack pageBackItem = Main.getConfigurator().readDefinedItem("pageback", "ARROW");
        ItemMeta pageBackItemMeta = backItem.getItemMeta();
        pageBackItemMeta.setDisplayName("previous back");
        pageBackItem.setItemMeta(pageBackItemMeta);
        options.setPageBackItem(pageBackItem);

        ItemStack pageForwardItem = Main.getConfigurator().readDefinedItem("pageforward", "ARROW");
        ItemMeta pageForwardItemMeta = backItem.getItemMeta();
        pageForwardItemMeta.setDisplayName("next page");
        pageForwardItem.setItemMeta(pageForwardItemMeta);
        options.setPageForwardItem(pageForwardItem);

        ItemStack cosmeticItem = Main.getConfigurator().readDefinedItem("shopcosmetic", "AIR");
        options.setCosmeticItem(cosmeticItem);

        options.setRows(6);
        options.setRender_actual_rows(6);
        options.setRender_offset(0);
        options.setRender_header_start(9);
        options.setRender_footer_start(600);
        options.setItems_on_row(9);
        options.setShowPageNumber(false);
        options.setInventoryType(InventoryType.valueOf(Main.getConfigurator().config.getString("shop.inventory-type", "CHEST")));

        options.setPrefix(Hypixelify.getConfigurator().config.getString("shop-name", "[SBAHypixelify] Shop"));
        options.setGenericShop(true);
        options.setGenericShopPriceTypeRequired(true);
        options.setAnimationsEnabled(true);
        options.registerPlaceholder("team", (key, player, arguments) -> {
            GamePlayer gPlayer = Main.getPlayerGameProfile(player);
            CurrentTeam team = gPlayer.getGame().getPlayerTeam(gPlayer);
            if (arguments.length > 0) {
                String fa = arguments[0];
                switch (fa) {
                    case "color":
                        return team.teamInfo.color.name();
                    case "chatcolor":
                        return team.teamInfo.color.chatColor.toString();
                    case "maxplayers":
                        return Integer.toString(team.teamInfo.maxPlayers);
                    case "players":
                        return Integer.toString(team.players.size());
                    case "hasBed":
                        return Boolean.toString(team.isBed);
                }
            }
            return team.getName();
        });
        options.registerPlaceholder("spawner", (key, player, arguments) -> {
            GamePlayer gPlayer = Main.getPlayerGameProfile(player);
            Game game = gPlayer.getGame();
            if (arguments.length > 2) {
                String upgradeBy = arguments[0];
                String upgrade = arguments[1];
                UpgradeStorage upgradeStorage = UpgradeRegistry.getUpgrade("spawner");
                if (upgradeStorage == null) {
                    return null;
                }
                List<Upgrade> upgrades = null;
                switch (upgradeBy) {
                    case "name":
                        upgrades = upgradeStorage.findItemSpawnerUpgrades(game, upgrade);
                        break;
                    case "team":
                        upgrades = upgradeStorage.findItemSpawnerUpgrades(game, game.getPlayerTeam(gPlayer));
                        break;
                }

                if (upgrades != null && !upgrades.isEmpty()) {
                    String what = "level";
                    if (arguments.length > 3) {
                        what = arguments[2];
                    }
                    double heighest = Double.MIN_VALUE;
                    switch (what) {
                        case "level":
                            for (Upgrade upgrad : upgrades) {
                                if (upgrad.getLevel() > heighest) {
                                    heighest = upgrad.getLevel();
                                }
                            }
                            return String.valueOf(heighest);
                        case "initial":
                            for (Upgrade upgrad : upgrades) {
                                if (upgrad.getInitialLevel() > heighest) {
                                    heighest = upgrad.getInitialLevel();
                                }
                            }
                            return String.valueOf(heighest);
                    }
                }
            }
            return "";
        });

        loadNewShop("default", null, true);
    }

    private static String getNameOrCustomNameOfItem(ItemStack stack) {
        try {
            if (stack.hasItemMeta()) {
                ItemMeta meta = stack.getItemMeta();
                if (meta == null) {
                    return "";
                }

                if (meta.hasDisplayName()) {
                    return meta.getDisplayName();
                }
                if (meta.hasLocalizedName()) {
                    return meta.getLocalizedName();
                }
            }
        } catch (Throwable ignored) {
        }

        String normalItemName = stack.getType().name().replace("_", " ").toLowerCase();
        String[] sArray = normalItemName.split(" ");
        StringBuilder stringBuilder = new StringBuilder();

        for (String s : sArray) {
            stringBuilder.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    public void show(Player player, GameStore store) {
        try {
            boolean parent = true;
            String file = null;
            if (store != null) {
                parent = store.getUseParent();
                file = store.getShopFile();
            }
            if (file != null) {
                if (file.endsWith(".yml")) {
                    file = file.substring(0, file.length() - 4);
                }
                String name = (parent ? "+" : "-") + file;
                if (!shopMap.containsKey(name)) {
                    if (Main.getConfigurator().config.getBoolean("turnOnExperimentalGroovyShop", false) && new File(Main.getInstance().getDataFolder(), file + ".groovy").exists()) {
                        loadNewShop(name, file + ".groovy", parent);
                    } else {
                        loadNewShop(name, file + ".yml", parent);
                    }
                }
                SimpleInventories shop = shopMap.get(name);
                shop.openForPlayer(player);
            } else {
                shopMap.get("default").openForPlayer(player);
            }
        } catch (Throwable ignored) {
            player.sendMessage(" Your shop.yml is invalid! Check it out or contact us on Discord");
        }
    }

    @EventHandler
    public void onGeneratingItem(GenerateItemEvent event) {
        if (!shopMap.containsValue(event.getFormat())) {
            return;
        }

        PlayerItemInfo item = event.getInfo();
        Player player = event.getPlayer();
        Game game = Main.getPlayerGameProfile(player).getGame();
        MapReader reader = item.getReader();
        if (reader.containsKey("price") && reader.containsKey("price-type")) {
            int price = reader.getInt("price");
            ItemSpawnerType type = Main.getSpawnerType((reader.getString("price-type")).toLowerCase());
            if (type == null) {
                return;
            }

            //
            boolean enabled = Main.getConfigurator().config.getBoolean("lore.generate-automatically", true);
            enabled = reader.getBoolean("generate-lore", enabled);

            List<String> loreText = reader.getStringList("generated-lore-text",
                    Main.getConfigurator().config.getStringList("lore.text"));


            String nprice = Integer.toString(price);

            String prop = null;

            if (event.getInfo().getProperties() != null && !event.getInfo().getProperties().isEmpty()) {
                for (ItemProperty property : event.getInfo().getProperties()) {
                    if (property.hasName()) {
                        if (property.getPropertyName().equalsIgnoreCase("protection") || property.getPropertyName().equalsIgnoreCase("sharpness")) {
                            prop = property.getPropertyName();
                            break;
                        }
                    }
                }
            }


            if (prop != null) {
                if (event.getStack() != null && prop.equalsIgnoreCase("protection") && event.getStack().getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) <= 4) {
                    ItemStack shop = ShopUtil.shopEnchants(event.getStack(), Objects.requireNonNull(event.getPlayer().getInventory().getBoots()), Enchantment.PROTECTION_ENVIRONMENTAL);
                    event.setStack(shop);
                    nprice = Integer.toString(Prices.get(shop.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL)));
                } else if (event.getStack() != null && prop.equalsIgnoreCase("sharpness") && event.getStack().getEnchantmentLevel(Enchantment.DAMAGE_ALL) <= 4) {
                    ItemStack sword = null;
                    for (ItemStack i : player.getInventory().getContents()) {
                        if (i != null && i.getType().name().endsWith("SWORD")) {
                            sword = i;
                            break;
                        }
                    }
                    assert sword != null;
                    ItemStack shop = ShopUtil.shopEnchants(event.getStack(), sword, Enchantment.DAMAGE_ALL);
                    event.setStack(shop);
                    nprice = Integer.toString(Prices.get(shop.getEnchantmentLevel(Enchantment.DAMAGE_ALL)));
                }
            }

            if (enabled) {
                ItemStack stack = event.getStack();
                ItemMeta stackMeta = stack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if (stackMeta.hasLore()) {
                    lore = stackMeta.getLore();
                }
                for (String s : loreText) {
                    s = s.replaceAll("%price%", nprice);
                    s = s.replaceAll("%resource%", type.getItemName());
                    s = s.replaceAll("%amount%", Integer.toString(stack.getAmount()));
                    assert lore != null;
                    lore.add(s);
                }
                stackMeta.setLore(lore);
                stack.setItemMeta(stackMeta);
                event.setStack(stack);
            }


            if (item.hasProperties()) {
                for (ItemProperty property : item.getProperties()) {
                    if (property.hasName()) {
                        ItemStack newItem = event.getStack();
                        BedwarsApplyPropertyToDisplayedItem applyEvent = new BedwarsApplyPropertyToDisplayedItem(game,
                                player, newItem, property.getReader(player, item).convertToMap());
                        Main.getInstance().getServer().getPluginManager().callEvent(applyEvent);

                        event.setStack(newItem);
                    }
                }
            }


        }
    }

    @EventHandler
    public void onPreAction(PreActionEvent event) {
        if (!shopMap.containsValue(event.getFormat()) || event.isCancelled()) {
            return;
        }

        if (!Main.isPlayerInGame(event.getPlayer())) {
            event.setCancelled(true);
        }

        if (Main.getPlayerGameProfile(event.getPlayer()).isSpectator) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onShopOpen(BedwarsOpenShopEvent event) {
        if (Main.getPlayerGameProfile(event.getPlayer()).isSpectator) return;
        if (Hypixelify.getConfigurator().config.getBoolean("store.replace-store-with-hypixelstore", true)) {
            event.setResult(Result.DISALLOW_THIRD_PARTY_SHOP);
            this.show(event.getPlayer(), event.getStore());
        }
    }

    @EventHandler
    public void onShopTransaction(ShopTransactionEvent event) {
        if (!shopMap.containsValue(event.getFormat()) || event.isCancelled()) {
            return;
        }

        MapReader reader = event.getItem().getReader();
        if (reader.containsKey("upgrade")) {
            handleUpgrade(event);
        } else {
            handleBuy(event);
        }
    }

    @EventHandler
    public void onApplyPropertyToBoughtItem(BedwarsApplyPropertyToItem event) {
        if (event.getPropertyName().equalsIgnoreCase("applycolorbyteam")
                || event.getPropertyName().equalsIgnoreCase("transform::applycolorbyteam")) {
            Player player = event.getPlayer();
            CurrentTeam team = (CurrentTeam) event.getGame().getTeamOfPlayer(player);

            if (Main.getConfigurator().config.getBoolean("automatic-coloring-in-shop")) {
                event.setStack(Main.applyColor(team.teamInfo.color, event.getStack()));
            }
        }

        try {
            if (event.getPropertyName().equalsIgnoreCase("sharpness")) {
                ItemStack sword = null;
                for (ItemStack i : event.getPlayer().getInventory().getContents()) {
                    if (i != null && i.getType().name().endsWith("SWORD")) {
                        sword = i;
                        break;
                    }
                }
                assert sword != null;
                ItemStack shop = ShopUtil.shopEnchants(event.getStack(), sword, Enchantment.DAMAGE_ALL);
                event.getStack().addEnchantments(shop.getEnchantments());
            }
            if (event.getPropertyName().equalsIgnoreCase("protection")) {
                ItemStack shop = ShopUtil.shopEnchants(event.getStack(), Objects.requireNonNull(event.getPlayer().getInventory().getBoots()), Enchantment.PROTECTION_ENVIRONMENTAL);
                event.getStack().addEnchantments(shop.getEnchantments());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNewShop(String name, String fileName, boolean useParent) {
        SimpleInventories format = new SimpleInventories(options);
        try {
            if (useParent) {
                String shopFileName = "shop.yml";
                if (Main.isLegacy()) {
                    shopFileName = "legacy-shop.yml";
                }
                if (Main.getConfigurator().config.getBoolean("turnOnExperimentalGroovyShop", false)) {
                    shopFileName = "shop.groovy";
                }
                format.loadFromDataFolder(Hypixelify.getInstance().getDataFolder(), shopFileName);
            }
            if (fileName != null) {
                if (fileName.equalsIgnoreCase("shop.yml") && Main.isLegacy())
                    fileName = "legacy-shop.yml";

                format.loadFromDataFolder(Hypixelify.getInstance().getDataFolder(), fileName);
            }
        } catch (Exception ignored) {
            Bukkit.getLogger().severe("Wrong shop.yml configuration!");
            Bukkit.getLogger().severe("Your villagers won't work, check validity of your YAML!");
        }

        format.generateData();
        shopMap.put(name, format);
    }

    public void buystack(ItemStack newItem, ShopTransactionEvent event) {
        Player player = event.getPlayer();
        HashMap<Integer, ItemStack> noFit = player.getInventory().addItem(newItem);
        if (!noFit.isEmpty()) {
            noFit.forEach((i, stack) -> player.getLocation().getWorld().dropItem(player.getLocation(), stack));
        }
    }

    public void sellstack(ItemStack newItem, ShopTransactionEvent event) {
        Player player = event.getPlayer();
        player.getInventory().removeItem(newItem);
    }

    @EventHandler
    public void onPlayerToolUpgrade(PlayerToolUpgradeEvent e) {
        Player player = e.getPlayer();
        ItemStack newItem = e.getUpgradedItem();
        RunningTeam team = e.getTeam();
        String name = e.getName();
        org.screamingsandals.bedwars.api.game.Game game = e.getGame();

        if (name.equalsIgnoreCase("sharpness")) {
            if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "SWORD", Enchantment.DAMAGE_ALL)) {
                e.setCancelled(true);
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You Already have the greatest enchantment");
            }
        } else if (name.equalsIgnoreCase("efficiency")) {
            if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "PICKAXE", Enchantment.DIG_SPEED)) {
                e.setCancelled(true);
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You Already have a greater or equal enchantment in your pickaxe!");
            }
        }
        else if(name.equalsIgnoreCase("blindtrap")) {
            if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName())) {
                if(!Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).addTrap(team)){
                    player.sendMessage(ChatColor.RED + "You already purchased this trap!, wait for it to wear out");
                    e.setCancelled(true);
                } else{
                    team.getConnectedPlayers().forEach(pl-> sendTitle(pl, "§6Blindness Trap has been purchased!", "", 20, 40, 20));
                }
            } else{
                e.setCancelled(true);
                player.sendMessage(ChatColor.RED + "An error has occured");
            }
        }


        else if (name.equalsIgnoreCase("protection")) {
            if (Objects.requireNonNull(player.getInventory().getBoots()).getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) >= 4) {
                e.setCancelled(true);
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You already have the greatest enchantment.");
            } else {
                ShopUtil.addEnchantsToPlayerArmor(player, newItem);
                for (Player playerCheck : team.getConnectedPlayers()) {
                    ShopUtil.addEnchantsToPlayerArmor(playerCheck, newItem);
                    playerCheck.sendMessage(ChatColor.ITALIC + "" + ChatColor.RED + player.getName() + ChatColor.YELLOW + " has upgraded team protection");
                }
            }
        }
    }

    private void handleBuy(ShopTransactionEvent event) {
        Player player = event.getPlayer();
        Game game = Main.getPlayerGameProfile(event.getPlayer()).getGame();
        RunningTeam team = game.getTeamOfPlayer(player);
        ClickType clickType = event.getClickType();
        MapReader mapReader = event.getItem().getReader();
        String priceType = event.getType().toLowerCase();
        ItemSpawnerType type = Main.getSpawnerType(priceType);
        ItemStack newItem = event.getStack();

        int amount = newItem.getAmount();
        int price = event.getPrice();
        int inInventory = 0;

        if (mapReader.containsKey("currency-changer")) {
            String changeItemToName = mapReader.getString("currency-changer");
            ItemSpawnerType changeItemType;
            if (changeItemToName == null) {
                return;
            }

            changeItemType = Main.getSpawnerType(changeItemToName);
            if (changeItemType == null) {
                return;
            }

            newItem = changeItemType.getStack();
        }

        if (clickType.isShiftClick() && newItem.getMaxStackSize() > 1) {
            double priceOfOne = (double) price / amount;
            double maxStackSize;
            int finalStackSize;

            for (ItemStack itemStack : event.getPlayer().getInventory().getStorageContents()) {
                if (itemStack != null && itemStack.isSimilar(type.getStack())) {
                    inInventory = inInventory + itemStack.getAmount();
                }
            }
            if (Main.getConfigurator().config.getBoolean("sell-max-64-per-click-in-shop")) {
                maxStackSize = Math.min(inInventory / priceOfOne, newItem.getMaxStackSize());
            } else {
                maxStackSize = inInventory / priceOfOne;
            }

            finalStackSize = (int) maxStackSize;
            if (finalStackSize > amount) {
                price = (int) (priceOfOne * finalStackSize);
                newItem.setAmount(finalStackSize);
                amount = finalStackSize;
            }
        }

        ItemStack materialItem = type.getStack(price);

        boolean sharp = false, prot = false, efficiency = false, trap = false;

        if (event.hasPlayerInInventory(materialItem)) {
            if (event.hasProperties()) {
                for (ItemProperty property : event.getProperties()) {
                    if (property.hasName()) {
                        BedwarsApplyPropertyToBoughtItem applyEvent = new BedwarsApplyPropertyToBoughtItem(game, player,
                                newItem, property.getReader(player, event.getItem()).convertToMap());
                        Main.getInstance().getServer().getPluginManager().callEvent(applyEvent);

                        newItem = applyEvent.getStack();
                        sharp = property.getPropertyName().equalsIgnoreCase("sharpness");
                        prot = property.getPropertyName().equalsIgnoreCase("protection");
                        efficiency = property.getPropertyName().equalsIgnoreCase("efficiency");
                        trap = property.getPropertyName().equalsIgnoreCase("blindtrap");
                    }
                }
            }

            boolean shouldSellStack = true;

            //fix NPE when player disequips armor.
            if (player.getInventory().getBoots() == null) {
                player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
            }


            try {
                if (sharp || efficiency || prot || trap) {
                    String name = sharp ? "sharpness" : efficiency ? "efficiency" : trap ?  "blindtrap" : "protection";
                    PlayerToolUpgradeEvent e = new PlayerToolUpgradeEvent(player, newItem, name, team, game);
                    Bukkit.getServer().getPluginManager().callEvent(e);
                    if (e.isCancelled()) {
                        return;
                    }

                    sellstack(materialItem, event);
                    if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                        player.sendMessage(ChatColor.GREEN + "You purchased " + ChatColor.YELLOW + getNameOrCustomNameOfItem(newItem));
                    }
                    Sounds.playSound(player, player.getLocation(),
                            Main.getConfigurator().config.getString("sounds.on_item_buy"), Sounds.ENTITY_ITEM_PICKUP, 1, 1);
                    return;

                } else if (newItem.getType().name().endsWith("SWORD")) {

                    if (!player.getInventory().contains(newItem.getType())) {
                        ShopUtil.upgradeSwordOnPurchase(player, newItem);
                        buystack(newItem, event);
                    } else {
                        shouldSellStack = false;
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You've Already purchased the same sword!");
                    }
                } else if (newItem.getType().equals(player.getInventory().getBoots().getType())) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You've Already purchased the same armor");
                    shouldSellStack = false;
                } else if (newItem.getType().name().contains("BOOTS")) {
                    ShopUtil.buyArmor(player, newItem.getType(), newItem.getType().name());
                } else if (newItem.getType().name().endsWith("AXE")) {
                    ShopUtil.removeAxeOrPickaxe(player, newItem);
                    if (!player.getInventory().contains(newItem)) {
                        buystack(newItem, event);
                    } else {
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You've Already purchased the same " + newItem.getType().name().substring(newItem.getType().name().indexOf("_")).substring(1));
                        shouldSellStack = false;
                    }
                } else {
                    buystack(newItem, event);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (shouldSellStack) {
                sellstack(materialItem, event);
                if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                    player.sendMessage(Hypixelify.getConfigurator().config.getString("message.purchase","§aYou purchased &e")
                            .replace("{item}",  getNameOrCustomNameOfItem(newItem)));
                }
                Sounds.playSound(player, player.getLocation(),
                        Main.getConfigurator().config.getString("sounds.on_item_buy"), Sounds.ENTITY_ITEM_PICKUP, 1, 1);
            }



        } else {
            if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                player.sendMessage(Hypixelify.getConfigurator().config.getString("message.cannot-buy","§cYou don't have enough {price}")
                        .replace("{price}", priceType));
            }
        }
    }

    private void handleUpgrade(ShopTransactionEvent event) {
        Player player = event.getPlayer();
        Game game = Main.getPlayerGameProfile(event.getPlayer()).getGame();
        MapReader mapReader = event.getItem().getReader();
        String priceType = event.getType().toLowerCase();
        ItemSpawnerType itemSpawnerType = Main.getSpawnerType(priceType);

        MapReader upgradeMapReader = mapReader.getMap("upgrade");
        List<MapReader> entities = upgradeMapReader.getMapList("entities");
        String itemName = upgradeMapReader.getString("shop-name", "UPGRADE");

        int price = event.getPrice();
        boolean sendToAll = false;
        boolean isUpgrade = true;
        ItemStack materialItem = itemSpawnerType.getStack(price);


        if (event.hasPlayerInInventory(materialItem)) {
            sellstack(materialItem, event);
            for (MapReader mapEntity : entities) {
                String configuredType = mapEntity.getString("type");
                if (configuredType == null) {
                    return;
                }

                UpgradeStorage upgradeStorage = UpgradeRegistry.getUpgrade(configuredType);
                if (upgradeStorage != null) {

                    // variables
                    Team team = game.getTeamOfPlayer(event.getPlayer());
                    double addLevels = mapEntity.getDouble("add-levels",
                            mapEntity.getDouble("levels", 0) /* Old configuration */);
                    /* You shouldn't use it in entities */
                    if (mapEntity.containsKey("shop-name")) {
                        itemName = mapEntity.getString("shop-name");
                    }
                    sendToAll = mapEntity.getBoolean("notify-team", false);

                    List<Upgrade> upgrades = new ArrayList<>();

                    if (mapEntity.containsKey("spawner-name")) {
                        String customName = mapEntity.getString("spawner-name");
                        upgrades = upgradeStorage.findItemSpawnerUpgrades(game, customName);
                    } else if (mapEntity.containsKey("spawner-type")) {
                        String mapSpawnerType = mapEntity.getString("spawner-type");
                        ItemSpawnerType spawnerType = Main.getSpawnerType(mapSpawnerType);

                        upgrades = upgradeStorage.findItemSpawnerUpgrades(game, team, spawnerType);
                    } else if (mapEntity.containsKey("team-upgrade")) {
                        boolean upgradeAllSpawnersInTeam = mapEntity.getBoolean("team-upgrade");

                        if (upgradeAllSpawnersInTeam) {
                            upgrades = upgradeStorage.findItemSpawnerUpgrades(game, team);
                        }

                    } else if (mapEntity.containsKey("customName")) { // Old configuration
                        String customName = mapEntity.getString("customName");
                        upgrades = upgradeStorage.findItemSpawnerUpgrades(game, customName);
                    } else {
                        isUpgrade = false;
                        Debugger.warn("[BedWars]> Upgrade configuration is invalid.");
                    }

                    if (isUpgrade) {
                        BedwarsUpgradeBoughtEvent bedwarsUpgradeBoughtEvent = new BedwarsUpgradeBoughtEvent(game,
                                upgradeStorage, upgrades, player, addLevels);
                        Bukkit.getPluginManager().callEvent(bedwarsUpgradeBoughtEvent);

                        if (bedwarsUpgradeBoughtEvent.isCancelled()) {
                            continue;
                        }

                        if (upgrades.isEmpty()) {
                            continue;
                        }

                        for (Upgrade upgrade : upgrades) {
                            BedwarsUpgradeImprovedEvent improvedEvent = new BedwarsUpgradeImprovedEvent(game,
                                    upgradeStorage, upgrade, upgrade.getLevel(), upgrade.getLevel() + addLevels);
                            Bukkit.getPluginManager().callEvent(improvedEvent);
                        }
                    }
                }

                if (sendToAll) {
                    for (Player player1 : game.getTeamOfPlayer(event.getPlayer()).getConnectedPlayers()) {
                        if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                            player1.sendMessage(("buy_succes").replace("%item%", itemName).replace("%material%",
                                    price + " " + itemSpawnerType.getItemName()));
                        }
                        Sounds.playSound(player1, player1.getLocation(),
                                Main.getConfigurator().config.getString("sounds.on_upgrade_buy"),
                                Sounds.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }
                } else {
                    if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", true)) {
                        player.sendMessage(ChatColor.GREEN + "You purchased " + ChatColor.YELLOW + event.getStack().getI18NDisplayName());
                    }
                    Sounds.playSound(player, player.getLocation(),
                            Main.getConfigurator().config.getString("sounds.on_upgrade_buy"),
                            Sounds.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
        } else {
            if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                player.sendMessage(ChatColor.RED + "you don't have enough " + priceType);
            }
        }
    }
}
