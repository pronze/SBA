package io.pronze.hypixelify.inventories;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.api.events.ApplyPropertyToItemEvent;
import io.pronze.hypixelify.api.events.PlayerToolUpgradeEvent;
import io.pronze.hypixelify.message.Messages;
import io.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent.Result;
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
import org.screamingsandals.bedwars.lib.sgui.utils.MapReader;
import org.screamingsandals.bedwars.utils.Debugger;
import org.screamingsandals.bedwars.utils.Sounds;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class CustomShop implements Listener {

    private final Map<Integer, Integer> Prices = new HashMap<>();
    private final Map<String, SimpleInventories> shopMap = new HashMap<>();
    private final Options options = new Options(Main.getInstance());

    private final List<String> const_properties = Arrays.asList(
            "sharpness",
            "protection",
            "blindtrap",
            "healpool",
            "dragon"
    );

    public CustomShop() {

        Prices.put(0, SBAHypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-I", 4));
        Prices.put(1, SBAHypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-I", 4));
        Prices.put(2, SBAHypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-II", 8));
        Prices.put(3, SBAHypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-III", 12));
        Prices.put(4, SBAHypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-IV", 16));

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

        options.setPrefix(SBAHypixelify.getConfigurator().getString("shop-name", "[SBAHypixelify] Shop"));
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

        loadNewShop("default", "shop.yml", false);
    }

    private static String getNameOrCustomNameOfItem(ItemStack stack) {
        try {
            if (stack.hasItemMeta()) {
                final ItemMeta meta = stack.getItemMeta();
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

        final var normalItemName = stack.getType().name().replace("_", " ").toLowerCase();
        final var sArray = normalItemName.split(" ");
        final var stringBuilder = new StringBuilder();

        Arrays.stream(sArray)
                .forEach(s-> {
                    stringBuilder.append(Character.toUpperCase(s.charAt(0)))
                            .append(s.substring(1)).append(" ");
                });
        return stringBuilder.toString().trim();
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    public void show(Player player, org.screamingsandals.bedwars.api.game.GameStore store) {
        try {
            boolean parent = true;
            String file = null;
            if (store != null) {
                parent = (boolean) store.getClass().getMethod("getUseParent").invoke(store);
                file = (String) store.getClass().getMethod("getShopFile").invoke(store);
            }
            if (file != null) {
                if (file.endsWith(".yml")) {
                    file = file.substring(0, file.length() - 4);
                }
                String name = (parent ? "+" : "-") + file;
                if (!shopMap.containsKey(name)) {
                    if (Main.getConfigurator().config.getBoolean("turnOnExperimentalGroovyShop", false) && new File(SBAHypixelify.getInstance().getDataFolder(), file + ".groovy").exists()) {
                        loadNewShop(name, file + ".groovy", parent);
                    } else {
                        loadNewShop(name, file + ".yml", parent);
                    }
                }
                final SimpleInventories shop = shopMap.get(name);
                shop.openForPlayer(player);
            } else {
                shopMap.get("default").openForPlayer(player);
            }
        } catch (Throwable e) {
            player.sendMessage(" Your shop.yml is invalid! Check it out or contact us on Discord");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onGeneratingItem(GenerateItemEvent event) {
        if (!shopMap.containsValue(event.getFormat())) {
            return;
        }

        final var item = event.getInfo();
        final var player = event.getPlayer();
        final var game = Main.getPlayerGameProfile(player).getGame();
        final var reader = item.getReader();
        final var gameStorage = SBAHypixelify.getGamestorage(game);
        final var runningTeam = game.getTeamOfPlayer(player);

        if (reader.containsKey("price") && reader.containsKey("price-type")) {
            final var price = reader.getInt("price");
            final var type = Main.getSpawnerType((reader.getString("price-type")).toLowerCase());
            if (type == null) {
                return;
            }

            final var eventStack = event.getStack();
            final var typeName = eventStack.getType().name();

            if (gameStorage != null) {
                if (typeName.endsWith("SWORD")) {
                    int sharpness = gameStorage.getSharpness(runningTeam.getName());
                    if (sharpness != 0) {
                        eventStack.addEnchantment(Enchantment.DAMAGE_ALL, sharpness);
                    }
                }
                if (typeName.endsWith("BOOTS")) {
                    int protection = gameStorage.getProtection(runningTeam.getName());
                    if (protection != 0) {
                        eventStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protection);
                    }
                }
                event.setStack(eventStack);
            }

            event.setStack(setLores(event.getStack(), reader, String.valueOf(price)));

            if (item.hasProperties()) {
                item.getProperties()
                        .stream()
                        .filter(ItemProperty::hasName)
                        .forEach(property-> {
                            final var newItem = event.getStack();
                            final var applyPropertyEvent =
                                    !const_properties.contains(property.getPropertyName().toLowerCase()) ?
                                    new ApplyPropertyToItemEvent(game, player, newItem,
                                            property.getReader(player, item).convertToMap(), reader) :
                                    new BedwarsApplyPropertyToDisplayedItem(game, player, newItem, property.getReader(player).convertToMap());
                            Main.getInstance().getServer().getPluginManager().callEvent(applyPropertyEvent);
                            event.setStack(newItem);
                        });
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
        final var player = event.getPlayer();
        if (Main.getPlayerGameProfile(player).isSpectator) return;
        if (SBAHypixelify.getConfigurator().config.getBoolean("store.replace-store-with-hypixelstore", true)) {
            event.setResult(Result.DISALLOW_UNKNOWN);
            this.show(player, event.getStore());
        }
    }

    @EventHandler
    public void onShopTransaction(ShopTransactionEvent event) {
        if (!shopMap.containsValue(event.getFormat()) || event.isCancelled()) {
            return;
        }
        final var reader = event.getItem().getReader();
        if (reader.containsKey("upgrade")) {
            handleUpgrade(event);
        } else {
            handleBuy(event);
        }
    }

    public ItemStack setLores(ItemStack stack, MapReader reader, String price){
        var loreEnabled = Main.getConfigurator().config
                .getBoolean("lore.generate-automatically", true);
        if (loreEnabled) {
            final var loreText = reader.getStringList("generated-lore-text",
                    Main.getConfigurator().config.getStringList("lore.text"));
            final var type = Main.getSpawnerType((reader.getString("price-type")).toLowerCase());
            final var stackMeta = stack.getItemMeta();
            final var newLore = new ArrayList<String>();
            loreText.forEach(lore-> {
                newLore.add(lore
                .replace("%price%", String.valueOf(price))
                .replace("%resource%", type.getItemName())
                .replace("%amount%", Integer.toString(stack.getAmount())));
            });
            stackMeta.setLore(newLore);
            stack.setItemMeta(stackMeta);
        }
        return stack;
    }

    @EventHandler
    public void onApplyPropertyToItem(ApplyPropertyToItemEvent event) {
        String price = null;
        final var game = event.getGame();
        final var player = event.getPlayer();
        final var team = game.getTeamOfPlayer(player);
        final var propertyName = event.getPropertyName();
        final var stack = event.getStack();
        final var gameStorage = SBAHypixelify.getGamestorage(game);

        if (gameStorage == null) {
            return;
        }

        if (propertyName.equalsIgnoreCase("sharpness")
        || propertyName.equalsIgnoreCase("protection")) {
            final var isSharp = propertyName.equalsIgnoreCase("sharpness");
            final var enchant = isSharp
                    ? Enchantment.DAMAGE_ALL : Enchantment.PROTECTION_ENVIRONMENTAL;

            final var level = isSharp ? gameStorage.getSharpness(team.getName()) + 1 :
                    gameStorage.getProtection(team.getName()) + 1;

            if (level >= 5) {
                stack.removeEnchantment(enchant);
                stack.setLore(SBAHypixelify.getConfigurator().getStringList("message.maximum-enchant-lore"));
                stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            } else {
                stack.addEnchantment(enchant, level);
                price = Integer.toString(Prices.get(level));
                event.setPrice(price);
                event.setStack(stack);
            }
        }

        if (event.getStack().hasItemMeta()) {
            final var lores = event.getStack().getItemMeta().getLore();
            if (lores != null) {
                for (var lore : lores) {
                    if (lore.contains("Maximum Enchant")) return;
                }
            }
        }

        if (price != null) {
            event.setStack(setLores(event.getStack(), event.getReader(), price));
        }
    }

    private void loadNewShop(String name, String fileName, boolean useParent) {
        final SimpleInventories format = new SimpleInventories(options);
        try {
            if (useParent) {
                String shopFileName = "shop.yml";
                if (Main.isLegacy()) {
                    shopFileName = "legacy-shop.yml";
                }
                if (Main.getConfigurator().config.getBoolean("turnOnExperimentalGroovyShop", false)) {
                    shopFileName = "shop.groovy";
                }
                format.loadFromDataFolder(SBAHypixelify.getInstance().getDataFolder(), shopFileName);
            }
            if (fileName != null) {
                if (Main.isLegacy()) {
                    if (fileName.equalsIgnoreCase("shop.yml"))
                        fileName = "legacy-shop.yml";
                    else if (fileName.equalsIgnoreCase("upgradeShop.yml"))
                        fileName = "legacy-upgradeShop.yml";
                }
                format.loadFromDataFolder(SBAHypixelify.getInstance().getDataFolder(), fileName);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Wrong shop.yml configuration!");
            Bukkit.getLogger().severe("Your villagers won't work, check validity of your YAML!");
            SBAHypixelify.debug(e.getMessage());
            SBAHypixelify.getInstance().getServer().getPluginManager().disablePlugin(SBAHypixelify.getInstance());
            return;
        }

        format.generateData();
        shopMap.put(name, format);
    }

    public void buyStack(ItemStack newItem, ShopTransactionEvent event) {
        final var player = event.getPlayer();

        if (newItem == null) {
            return;
        }

        if (SBAHypixelify.getConfigurator().config.getBoolean("experimental.reset-item-meta-on-purchase", false)) {
            final var meta = Bukkit.getServer().getItemFactory().getItemMeta(newItem.getType());
            final var enchants = newItem.getEnchantments();
            newItem.setItemMeta(meta);
            if (!enchants.isEmpty())
                newItem.addUnsafeEnchantments(enchants);
        }

        final var noFit = player.getInventory().addItem(newItem);

        if (!noFit.isEmpty()) {
            noFit.forEach((i, stack) -> player.getLocation().getWorld().dropItem(player.getLocation(), stack));
        }
    }

    public void sellstack(ItemStack newItem, ShopTransactionEvent event) {
        Player player = event.getPlayer();
        player.getInventory().removeItem(newItem);
    }

    //TODO: this needs to be rewritten
    @EventHandler
    public void onPlayerToolUpgrade(PlayerToolUpgradeEvent e) {
        final var player = e.getPlayer();
        final var newItem = e.getUpgradedItem();
        final var team = e.getTeam();
        final var name = e.getName();
        final var game = e.getGame();
        final var gameStorage = SBAHypixelify.getGamestorage(game);

        if (gameStorage == null) {
            e.setCancelled(true);
            player.sendMessage(Messages.ERROR_OCCURED);
            return;
        }

        if (name.equalsIgnoreCase("sharpness")) {
            if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "SWORD", Enchantment.DAMAGE_ALL)) {
                e.setCancelled(true);
                player.sendMessage(Messages.message_greatest_enchantment);
            } else {
                var level = newItem.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                var price = Prices.get(level);
                var materialItem = e.getStackFromPrice(price);
                if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                    Objects.requireNonNull(SBAHypixelify.getGamestorage(game)).setSharpness(team.getName()
                            , level);
                }
                e.setPrice(Integer.toString(price));
            }
        }//else if (name.equalsIgnoreCase("dragon")) {
         //  if (gameStorage.isTrapEnabled(team)) {
         //      player.sendMessage(Messages.trap_timeout_message);
         //      e.setCancelled(true);
         //  } else {
         //      gameStorage.setDragon(team, true);
         //      team.getConnectedPlayers().forEach(pl -> sendTitle(pl, Messages.dragonTrapPurchased, "", 20, 40, 20));
         //  }
         //  }

        else if (name.equalsIgnoreCase("efficiency")) {
            if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "PICKAXE", Enchantment.DIG_SPEED)) {
                e.setCancelled(true);
                player.sendMessage(Messages.message_greatest_enchantment);
            }
        }

        else if (name.equalsIgnoreCase("blindtrap")) {
            if (SBAHypixelify.getGamestorage(game) == null) {
                e.setCancelled(true);
                player.sendMessage(Messages.ERROR_OCCURED);
                return;
            }
            if (Objects.requireNonNull(SBAHypixelify.getGamestorage(game)).isTrapEnabled(team)) {
                player.sendMessage(Messages.trap_timeout_message);
                e.setCancelled(true);
            } else {
                Objects.requireNonNull(SBAHypixelify.getGamestorage(game)).setTrap(team, true);
                team.getConnectedPlayers().forEach(pl -> sendTitle(pl, Messages.blindnessTrapPurchased, "", 20, 40, 20));
            }
        }

        else if (name.equalsIgnoreCase("healpool")) {
            if (SBAHypixelify.getGamestorage(game) == null) {
                e.setCancelled(true);
                player.sendMessage(Messages.ERROR_OCCURED);
                return;
            }
            if (Objects.requireNonNull(SBAHypixelify.getGamestorage(game)).isPoolEnabled(team)) {
                player.sendMessage(Messages.trap_timeout_message);
                e.setCancelled(true);
            } else {
                Objects.requireNonNull(SBAHypixelify.getGamestorage(game)).setPool(team, true);
                team.getConnectedPlayers().forEach(pl -> pl.sendMessage(Messages.message_purchase_heal_pool
                        .replace("{player}", player.getName())));
            }
        }

        else if (name.equalsIgnoreCase("protection")) {
            if (Objects.requireNonNull(player.getInventory().getBoots())
                    .getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) >= 4) {
                e.setCancelled(true);
                player.sendMessage("§c§l" + Messages.message_greatest_enchantment);
            } else {
                var level = newItem.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
                var price = Prices.get(level);
                var materialItem = e.getStackFromPrice(price);
                if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                    Objects.requireNonNull(SBAHypixelify.getGamestorage(game)).setProtection(team.getName()
                            , level);
                }
                e.setPrice(Integer.toString(price));
                ShopUtil.addEnchantsToPlayerArmor(player, newItem);
                for (Player playerCheck : team.getConnectedPlayers()) {
                    ShopUtil.addEnchantsToPlayerArmor(playerCheck, newItem);
                    playerCheck.sendMessage(Messages.message_upgrade_team_protection
                            .replace("{player}", player.getName()));
                }
            }
        }
    }

    private void handleBuy(ShopTransactionEvent event) {
        final var player = event.getPlayer();
        final var game = Main.getPlayerGameProfile(event.getPlayer()).getGame();
        final var team = game.getTeamOfPlayer(player);
        final var clickType = event.getClickType();
        final var mapReader = event.getItem().getReader();
        final var priceType = event.getType().toLowerCase();
        final var type = Main.getSpawnerType(priceType);
        var newItem = event.getStack();

        int amount = newItem.getAmount();
        int price = event.getPrice();
        int inInventory = 0;

        if (mapReader.containsKey("currency-changer")) {
            final var changeItemToName = mapReader.getString("currency-changer");
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

        var materialItem = type.getStack(price);

        String propName = null;

        if (event.hasPlayerInInventory(materialItem)) {
            if (event.hasProperties()) {
                AtomicReference<ItemStack> finalNewItem = new AtomicReference<>(newItem);
                AtomicReference<Integer> priceReference = new AtomicReference<>();
                AtomicReference<String> propertyReference = new AtomicReference<>();

                event.getProperties()
                        .stream()
                        .filter(ItemProperty::hasName)
                        .forEach(property-> {
                            final var propertyName = property.getPropertyName().toLowerCase();
                            final var isCustomProperty = const_properties.contains(propertyName);

                            final var propertyData = property.getReader(player, event.getItem()).convertToMap();

                            final var applyEvent = isCustomProperty ?
                                    new ApplyPropertyToItemEvent(game, player, finalNewItem.get(), propertyData, mapReader) :
                                    new BedwarsApplyPropertyToBoughtItem(game, player, finalNewItem.get(), propertyData);
                            SBAHypixelify.getInstance().getServer().getPluginManager().callEvent(applyEvent);

                            try {
                                final var stack = (ItemStack) applyEvent.getClass().getMethod("getStack", null).invoke(applyEvent);
                                finalNewItem.set(stack);

                                if (isCustomProperty) {
                                    final var appliedPrice = Integer.parseInt((String) applyEvent.getClass().getMethod("getPrice", null).invoke(applyEvent));
                                    priceReference.set(appliedPrice);
                                    propertyReference.set(propertyName);
                                }
                            } catch (Throwable ignored) {}
                        });

                final var newItemFromReference = finalNewItem.get();
                if (newItemFromReference != null) {
                    newItem = newItemFromReference;
                }

                if (priceReference.get() != null) {
                    materialItem = type.getStack(priceReference.get());
                }

                if (propertyReference.get() != null) {
                    propName = propertyReference.get();
                }
            }

            var shouldSellStack = true;
            final var typeName = newItem.getType().name();

            if (propName != null) {
                final var e = new PlayerToolUpgradeEvent(player, newItem, propName, team, game, type);
                Main.getInstance().getServer().getPluginManager().callEvent(e);
                if (e.isCancelled()) {
                    return;
                }
                if (e.getPrice() != null)
                    materialItem = type.getStack(Integer.parseInt(e.getPrice()));

                //since we are  setting the price to a different one on upgrade, we do the check again
                if (!event.hasPlayerInInventory(materialItem) &&
                        !Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                    player.sendMessage(Objects.requireNonNull(SBAHypixelify.getConfigurator().getString("message.cannot-buy", "§cYou don't have enough {price}"))
                            .replace("{price}", priceType));
                    return;
                }
                sellstack(materialItem, event);
                if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                    player.sendMessage("§aYou purchased §e" + getNameOrCustomNameOfItem(newItem));
                }
                Sounds.playSound(player, player.getLocation(),
                        Main.getConfigurator().config.getString("sounds.on_item_buy"), Sounds.ENTITY_ITEM_PICKUP, 1, 1);

                return;

            } else if (typeName.endsWith("SWORD")) {

                if (!player.getInventory().contains(newItem.getType())) {
                    ShopUtil.upgradeSwordOnPurchase(player, newItem, game);
                    buyStack(newItem, event);
                } else {
                    shouldSellStack = false;
                    player.sendMessage(Messages.already_purchased_thing
                                    .replace("{thing}", "Sword"));
                }
            } else if (player.getInventory().getBoots() != null
                    && newItem.getType().equals(player.getInventory().getBoots().getType())) {
                player.sendMessage(Messages.already_purchased_thing
                        .replace("{thing}", "Armor"));
                shouldSellStack = false;
            } else if (newItem.getType().name().contains("BOOTS")) {
                ShopUtil.buyArmor(player, newItem.getType(), newItem.getType().name(), game);
            } else if (newItem.getType().name().endsWith("AXE")) {
                if (!player.getInventory().contains(newItem)) {
                    ShopUtil.removeAxeOrPickaxe(player, newItem);
                    buyStack(newItem, event);
                } else {
                    player.sendMessage(Messages.already_purchased_thing
                            .replace("{thing}",
                                    newItem.getType().name().substring
                                            (newItem.getType().name().indexOf("_")).substring(1)));
                    shouldSellStack = false;
                }
            } else {
                buyStack(newItem, event);
            }

            if (shouldSellStack) {
                sellstack(materialItem, event);
                if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                    player.sendMessage(Objects.requireNonNull(SBAHypixelify.getConfigurator().getString("message.purchase", "§aYou purchased &e"))
                            .replace("{item}", getNameOrCustomNameOfItem(newItem)));
                }
                Sounds.playSound(player, player.getLocation(),
                        Main.getConfigurator().config.getString("sounds.on_item_buy"), Sounds.ENTITY_ITEM_PICKUP, 1, 1);
            }


        } else {
            if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                player.sendMessage(Objects.requireNonNull(SBAHypixelify.getConfigurator().getString("message.cannot-buy", "§cYou don't have enough {price}"))
                        .replace("{price}", priceType));
            }
        }
    }

    //TODO: bring in proper spawner upgrades later
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
                        player.sendMessage("§aYou purchased §e" + event.getStack().getI18NDisplayName());
                    }
                    Sounds.playSound(player, player.getLocation(),
                            Main.getConfigurator().config.getString("sounds.on_upgrade_buy"),
                            Sounds.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
        } else {
            if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                player.sendMessage("§cyou don't have enough " + priceType);
            }
        }
    }

}
