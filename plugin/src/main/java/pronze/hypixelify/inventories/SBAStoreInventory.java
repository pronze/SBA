package pronze.hypixelify.inventories;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToBoughtItem;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToDisplayedItem;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.api.upgrades.Upgrade;
import org.screamingsandals.bedwars.api.upgrades.UpgradeRegistry;
import org.screamingsandals.bedwars.api.upgrades.UpgradeStorage;
import org.screamingsandals.bedwars.commands.DumpCommand;
import org.screamingsandals.bedwars.config.MainConfig;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.bedwars.lib.material.Item;
import org.screamingsandals.bedwars.lib.material.builder.ItemFactory;
import org.screamingsandals.bedwars.lib.material.meta.EnchantmentHolder;
import org.screamingsandals.bedwars.lib.material.meta.EnchantmentMapping;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.player.PlayerWrapper;
import org.screamingsandals.bedwars.lib.sgui.SimpleInventoriesCore;
import org.screamingsandals.bedwars.lib.sgui.events.ItemRenderEvent;
import org.screamingsandals.bedwars.lib.sgui.events.OnTradeEvent;
import org.screamingsandals.bedwars.lib.sgui.events.PreClickEvent;
import org.screamingsandals.bedwars.lib.sgui.inventory.Include;
import org.screamingsandals.bedwars.lib.sgui.inventory.InventorySet;
import org.screamingsandals.bedwars.lib.utils.ConfigurateUtils;
import org.screamingsandals.bedwars.player.PlayerManager;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.game.IStoreInventory;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.ShopUtil;
import pronze.lib.core.Core;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.annotations.OnInit;
import pronze.lib.core.utils.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@AutoInitialize(listener = true)
public class SBAStoreInventory implements IStoreInventory, Listener {
    private final Map<String, InventorySet> shopMap = new HashMap<>();

    public static SBAStoreInventory getInstance() {
        return Core.getObjectFromClass(SBAStoreInventory.class);
    }

    /**
     * Method invoked on instantiation of this class.
     * Will save shop file to shops directory in case of it's absence and also load the default shops into the ShopMap.
     */
    @OnInit
    public void onCreation() {
        var shopFile = SBAHypixelify
                .getInstance()
                .getDataFolder()
                .toPath()
                .resolve("shops/shop.yml")
                .toFile();

        if (!shopFile.exists()) {
            SBAHypixelify
                    .getInstance()
                    .saveResource("shops/shop.yml", false);
            loadNewShop("default", null, true);
        }
    }

    @Override
    public Optional<InventorySet> getInventory(String key) {
        return Optional.ofNullable(shopMap.get(key));
    }

    @Override
    public void openForPlayer(PlayerWrapper player, GameStore store) {
        try {
            var parent = true;
            String fileName = null;
            if (store != null) {
                parent = store.getUseParent();
                fileName = store.getShopFile();
            }
            if (fileName != null) {
                var file = ShopUtil.normalizeShopFile(fileName);
                var name = (parent ? "+" : "-") + file.getAbsolutePath();
                if (!shopMap.containsKey(name)) {
                    loadNewShop(name, file, parent);
                }
                player.openInventory(shopMap.get(name));
            } else {
                player.openInventory(shopMap.get("default"));
            }
        } catch (Throwable ignored) {
            player.sendMessage("[BW] Your shop.yml/shop.groovy is invalid! Check it out or contact us on Discord.");
        }
    }

    public void loadNewShop(String name, File file, boolean useParent) {
        var inventorySet = SimpleInventoriesCore
                .builder()
                .genericShop(true)
                .genericShopPriceTypeRequired(true)
                .animationsEnabled(true)
                .categoryOptions(localOptionsBuilder ->
                        localOptionsBuilder
                                .backItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "normal-shop", "shopback"), "BARRIER"), itemBuilder ->
                                        itemBuilder.name(LanguageService.getInstance().get(MessageKeys.SHOP_PAGE_BACK).toComponent())
                                )
                                .pageBackItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "normal-shop", "pageback"), "ARROW"), itemBuilder ->
                                        itemBuilder.name(LanguageService.getInstance().get(MessageKeys.SHOP_PAGE_BACK).toComponent())
                                )
                                .pageForwardItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "normal-shop", "pageforward"), "BARRIER"), itemBuilder ->
                                        itemBuilder.name(LanguageService.getInstance().get(MessageKeys.SHOP_PAGE_FORWARD).toComponent())
                                )
                                .cosmeticItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "normal-shop", "shopcosmetic"), "AIR"))
                                .rows(SBAConfig.getInstance().node("shop", "normal-shop", "rows").getInt(4))
                                .renderActualRows(SBAConfig.getInstance().node("shop", "normal-shop", "render-actual-rows").getInt(6))
                                .renderOffset(SBAConfig.getInstance().node("shop", "normal-shop", "render-offset").getInt(9))
                                .renderHeaderStart(SBAConfig.getInstance().node("shop", "normal-shop", "render-header-start").getInt(0))
                                .renderFooterStart(SBAConfig.getInstance().node("shop", "normal-shop", "render-footer-start").getInt(45))
                                .itemsOnRow(SBAConfig.getInstance().node("shop", "normal-shop", "items-on-row").getInt(9))
                                .showPageNumber(SBAConfig.getInstance().node("shop", "normal-shop", "show-page-numbers").getBoolean(true))
                                .inventoryType(SBAConfig.getInstance().node("shop", "normal-shop", "inventory-type").getString("CHEST"))
                                .prefix(LanguageService.getInstance().get(MessageKeys.SHOP_NAME).toComponent())
                )

                // old shop format compatibility
                .variableToProperty("upgrade", "upgrade")
                .variableToProperty("generate-lore", "generateLore")
                .variableToProperty("generated-lore-text", "generatedLoreText")
                .variableToProperty("currency-changer", "currencyChanger")
                .render(this::onGeneratingItem)
                .preClick(this::onPreAction)
                .buy(this::onShopTransaction)
                .define("team", (key, player, playerItemInfo, arguments) -> {
                    var gPlayer = PlayerManager.getInstance().getPlayer(player);
                    var team = gPlayer.orElseThrow().getGame().getPlayerTeam(gPlayer.get());
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
                })
                .define("spawner", (key, player, playerItemInfo, arguments) -> {
                    var gPlayer = PlayerManager.getInstance().getPlayer(player);
                    Game game = gPlayer.orElseThrow().getGame();
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
                                upgrades = upgradeStorage.findItemSpawnerUpgrades(game, game.getTeamOfPlayer(player.as(Player.class)));
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
                })
                .call(categoryBuilder -> {
                    if (useParent) {
                        var shopFileName = "shop.yml";
                        categoryBuilder.include(shopFileName);
                    }
                    if (file != null) {
                        categoryBuilder.include(Include.of(file));
                    }
                })
                .getInventorySet();

        try {
            inventorySet.getMainSubInventory().process();
        } catch (Exception ex) {
            Bukkit.getLogger().warning("Wrong shop.yml configuration!");
            Bukkit.getLogger().warning("Check validity of your YAML!");
            ex.printStackTrace();
            loadDefault(inventorySet);
        }

        shopMap.put(name, inventorySet);
    }

    private void handleBuy(OnTradeEvent event) {
        var player = event.getPlayer().as(Player.class);

        var game = PlayerManager
                .getInstance()
                .getGameOfPlayer(event.getPlayer())
                .orElseThrow();

        var clickType = event.getClickType();
        var itemInfo = event.getItem();

        var price = event.getPrices().get(0);
        ItemSpawnerType type = Main.getSpawnerType(price.getCurrency().toLowerCase());

        var newItem = event.getStack();

        var amount = newItem.getAmount();
        var priceAmount = price.getAmount();
        int inInventory = 0;

        var currencyChanger = itemInfo.getFirstPropertyByName("currencyChanger");
        if (currencyChanger.isPresent()) {
            var changeItemToName = currencyChanger.get().getPropertyData().getString();
            ItemSpawnerType changeItemType;
            if (changeItemToName == null) {
                return;
            }

            changeItemType = Main.getSpawnerType(changeItemToName.toLowerCase());
            if (changeItemType == null) {
                return;
            }

            newItem = ItemFactory.build(changeItemType.getStack()).orElse(newItem);
        }

        var originalMaxStackSize = newItem.getMaterial().as(Material.class).getMaxStackSize();
        if (clickType.isShiftClick() && originalMaxStackSize > 1) {
            double priceOfOne = (double) priceAmount / amount;
            double maxStackSize;
            int finalStackSize;

            for (ItemStack itemStack : player.getInventory().getStorageContents()) {
                if (itemStack != null && itemStack.isSimilar(type.getStack())) {
                    inInventory = inInventory + itemStack.getAmount();
                }
            }
            if (MainConfig.getInstance().node("sell-max-64-per-click-in-shop").getBoolean()) {
                maxStackSize = Math.min(inInventory / priceOfOne, originalMaxStackSize);
            } else {
                maxStackSize = inInventory / priceOfOne;
            }

            finalStackSize = (int) maxStackSize;
            if (finalStackSize > amount) {
                priceAmount = (int) (priceOfOne * finalStackSize);
                newItem.setAmount(finalStackSize);
                amount = finalStackSize;
            }
        }

        var materialItem = ItemFactory
                .build(type.getStack(priceAmount))
                .orElseThrow();

        // purchase failed, player does not have enough resources to purchase
        if (!event.hasPlayerInInventory(materialItem)) {
            if (!SBAConfig.getInstance().node("shop", "removePurchaseMessages").getBoolean()) {
                LanguageService
                        .getInstance()
                        .get(MessageKeys.SHOP_PURCHASE_FAILED)
                        .replace("%item%", amount + "x " + ShopUtil.getNameOrCustomNameOfItem(newItem))
                        .replace("%material%", priceAmount + " " + type.getItemName())
                        .send(event.getPlayer());
            }
            return;
        }

        for (var property : itemInfo.getProperties()) {
            if (property.hasName()) {
                var converted = ConfigurateUtils.raw(property.getPropertyData());
                if (!(converted instanceof Map)) {
                    converted = DumpCommand.nullValuesAllowingMap("value", converted);
                }
                //noinspection unchecked
                var propertyData = (Map<String, Object>) converted;
                var applyEvent = new BedwarsApplyPropertyToBoughtItem(game, player, newItem.as(ItemStack.class), property.getPropertyName(), propertyData);
                Bukkit.getServer().getPluginManager().callEvent(applyEvent);
                newItem = ItemFactory
                        .build(applyEvent.getStack())
                        .orElse(newItem);

            }
        }

        var shouldBuyStack = true;

        var gameStorage = SBAHypixelify
                .getInstance()
                .getGameStorage(game)
                .orElseThrow();

        final var typeName = newItem.getMaterial().getPlatformName();
        final var team = game.getTeamOfPlayer(player);

        final var afterUnderscore = typeName.substring(typeName.contains("_") ? typeName.indexOf("_") + 1 : 0);
        /**
         * Apply enchants to item here according to TeamUpgrades.
         */
        switch (afterUnderscore.toLowerCase()) {
            case "sword":
                final var sharpness = gameStorage.getSharpness(team.getName());
                if (sharpness > 0 && sharpness < 5) {
                    newItem.addEnchant(new EnchantmentHolder(EnchantmentMapping
                            .resolve(Enchantment.DAMAGE_ALL).orElseThrow().getPlatformName(), sharpness));
                }
            case "boots":
            case "chestplate":
            case "helmet":
            case "leggings":
                shouldBuyStack = false;
                ShopUtil.buyArmor(player, newItem.as(ItemStack.class).getType(), gameStorage, game);
                break;
            case "pickaxe":
                final var efficiency = gameStorage.getEfficiency(team.getName());
                if (efficiency > 0 && efficiency < 5) {
                    newItem.addEnchant(new EnchantmentHolder(EnchantmentMapping
                            .resolve(Enchantment.DIG_SPEED).orElseThrow().getPlatformName(), efficiency));
                }
        }

        event.sellStack(materialItem);

        if (shouldBuyStack) {
            List<Item> notFit = event.buyStack(newItem);
            if (!notFit.isEmpty()) {
                notFit.forEach(stack -> player.getLocation().getWorld().dropItem(player.getLocation(), stack.as(ItemStack.class)));
            }
        }

        if (!SBAConfig.getInstance().node("shop", "removePurchaseMessages").getBoolean()) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.SHOP_PURCHASE_SUCCESS)
                    .replace("%item%", amount + "x " + ShopUtil.getNameOrCustomNameOfItem(newItem))
                    .replace("%material%", priceAmount + " " + type.getItemName())
                    .send(event.getPlayer());
        }
    }

    @SneakyThrows
    private void loadDefault(InventorySet inventorySet) {
        inventorySet.getMainSubInventory().dropContents();
        inventorySet.getMainSubInventory().getWaitingQueue().add(Include.of(Path.of(SBAStoreInventory.class.getResource("/shops/shop.yml").toURI())));
        inventorySet.getMainSubInventory().process();
    }

    private void onShopTransaction(OnTradeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        handleBuy(event);
    }

    private void onPreAction(PreClickEvent event) {
        if (event.isCancelled()) {
            return;
        }

        var player = event.getPlayer();
        if (!PlayerManager.getInstance().isPlayerInGame(player)) {
            event.setCancelled(true);
        }

        if (PlayerManager.getInstance().getPlayer(player).orElseThrow().isSpectator) {
            event.setCancelled(true);
        }
    }

    private void onGeneratingItem(ItemRenderEvent event) {
        var itemInfo = event.getItem();
        var item = itemInfo.getStack();
        var player = event.getPlayer().as(Player.class);
        var game = PlayerManager.getInstance().getGameOfPlayer(event.getPlayer());
        var prices = itemInfo.getOriginal().getPrices();
        if (!prices.isEmpty()) {
            var priceObject = prices.get(0);
            var price = priceObject.getAmount();
            var type = Main.getSpawnerType(priceObject.getCurrency().toLowerCase());
            if (type == null) {
                return;
            }
            ShopUtil.setLore(item, itemInfo, String.valueOf(price), type);
        }

        ShopUtil.applyTeamUpgradeEnchantsToItem(item, event);

        itemInfo.getProperties().forEach(property -> {
            if (property.hasName()) {
                var converted = ConfigurateUtils.raw(property.getPropertyData());
                if (!(converted instanceof Map)) {
                    converted = DumpCommand.nullValuesAllowingMap("value", converted);
                }

                //noinspection unchecked
                var applyEvent = new BedwarsApplyPropertyToDisplayedItem(game.orElse(null),
                        player, item.as(ItemStack.class), property.getPropertyName(), (Map<String, Object>) converted);
                Bukkit.getServer().getPluginManager().callEvent(applyEvent);

                event.setStack(ItemFactory.build(applyEvent.getStack()).orElse(item));
            }
        });
    }

    @EventHandler
    public void onBedWarsOpenShop(BedwarsOpenShopEvent event) {
        final var shopFile = event.getStore().getShopFile();
        if ((shopFile != null && shopFile.equalsIgnoreCase("shop.yml") )|| event.getStore().getUseParent()) {
            if (SBAConfig.getInstance().node("shop", "normal-shop", "enabled").getBoolean()) {
                event.setResult(BedwarsOpenShopEvent.Result.DISALLOW_UNKNOWN);
                Logger.trace("Player: {} has opened store!", event.getPlayer().getName());
                openForPlayer(PlayerMapper.wrapPlayer(event.getPlayer()), (GameStore) event.getStore());
            }
        }
    }
}
