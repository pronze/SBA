package pronze.sba.inventories;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToDisplayedItem;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToItem;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.api.upgrades.Upgrade;
import org.screamingsandals.bedwars.api.upgrades.UpgradeRegistry;
import org.screamingsandals.bedwars.api.upgrades.UpgradeStorage;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.lib.material.Item;
import org.screamingsandals.lib.material.builder.ItemFactory;
import org.screamingsandals.lib.material.meta.EnchantmentHolder;
import org.screamingsandals.lib.material.meta.EnchantmentMapping;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.ConfigurateUtils;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.simpleinventories.SimpleInventoriesCore;
import org.screamingsandals.simpleinventories.events.ItemRenderEvent;
import org.screamingsandals.simpleinventories.events.OnTradeEvent;
import org.screamingsandals.simpleinventories.events.PreClickEvent;
import org.screamingsandals.simpleinventories.inventory.Include;
import org.screamingsandals.simpleinventories.inventory.InventorySet;
import pronze.sba.SBA;
import pronze.sba.MessageKeys;
import pronze.sba.game.IStoreInventory;
import pronze.sba.game.StoreType;
import pronze.sba.wrapper.PlayerWrapper;
import pronze.sba.config.SBAConfig;
import pronze.sba.lib.lang.LanguageService;
import pronze.sba.utils.Logger;
import pronze.sba.utils.ShopUtil;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service(dependsOn = {
        SimpleInventoriesCore.class,
})
public class SBAStoreInventory implements IStoreInventory, Listener {
    private final Map<String, InventorySet> shopMap = new HashMap<>();

    public static SBAStoreInventory getInstance() {
        return ServiceManager.get(SBAStoreInventory.class);
    }

    @OnPostEnable
    public void onPostEnable() {
        var shopFile = SBA
                .getPluginInstance()
                .getDataFolder()
                .toPath()
                .resolve("shops/shop.yml")
                .toFile();

        if (!shopFile.exists()) {
            SBA
                    .getInstance()
                    .saveResource("shops/shop.yml", false);
            loadNewShop("default", null, true);
        }
    }

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
                    var gPlayer = Main.getPlayerGameProfile(player.as(Player.class));
                    var team = gPlayer.getGame().getPlayerTeam(gPlayer);
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
                    var gPlayer = Main.getPlayerGameProfile(player.as(Player.class));
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
                    var pathStr = SBA.getPluginInstance().getDataFolder().getAbsolutePath();
                    pathStr = pathStr + "/shops/" + (file != null ? file.getName() : "shop.yml");
                    categoryBuilder.include(Include.of(Paths.get(pathStr)));
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

        var game = Main.getInstance().getGameOfPlayer(player);

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
            if (Main.getInstance().getConfig().getBoolean("sell-max-64-per-click-in-shop")) {
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
                        .replace("<item>", amount + "x " + ShopUtil.getNameOrCustomNameOfItem(newItem))
                        .replace("<material>", priceAmount + " " + type.getItemName())
                        .send(event.getPlayer());
            }
            return;
        }

        for (var property : itemInfo.getProperties()) {
            if (property.hasName()) {
                var converted = ConfigurateUtils.raw(property.getPropertyData());
                if (!(converted instanceof Map)) {
                    converted = ShopUtil.nullValuesAllowingMap("value", converted);
                }
                //noinspection unchecked
                var propertyData = (Map<String, Object>) converted;
                var applyEvent = new BedwarsApplyPropertyToItem(game, player, newItem.as(ItemStack.class), propertyData);
                SBA.getPluginInstance().getServer().getPluginManager().callEvent(applyEvent);
                newItem = ItemFactory
                        .build(applyEvent.getStack())
                        .orElse(newItem);
            }
        }

        var shouldBuyStack = true;

        var gameStorage = SBA
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
                    .replace("<item>", amount + "x " + ShopUtil.getNameOrCustomNameOfItem(newItem))
                    .replace("<material>", priceAmount + " " + type.getItemName())
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

        var player = event.getPlayer().as(Player.class);
        if (!Main.isPlayerInGame(player)) {
            event.setCancelled(true);
        }

        if (Main.getPlayerGameProfile(player).isSpectator) {
            event.setCancelled(true);
        }
    }

    private void onGeneratingItem(ItemRenderEvent event) {
        var itemInfo = event.getItem();
        var item = itemInfo.getStack();
        var player = event.getPlayer().as(Player.class);
        var game = Main.getInstance().getGameOfPlayer(player);

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

        ShopUtil.applyTeamUpgradeEnchantsToItem(item, event, StoreType.NORMAL);

        itemInfo.getProperties().forEach(property -> {
            if (property.hasName()) {
                var converted = ConfigurateUtils.raw(property.getPropertyData());
                if (!(converted instanceof Map)) {
                    converted = ShopUtil.nullValuesAllowingMap("value", converted);
                }


                //noinspection unchecked
                var applyEvent = new BedwarsApplyPropertyToDisplayedItem(game,
                        player, item.as(ItemStack.class), (Map<String, Object>) converted);
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
                openForPlayer(PlayerMapper.wrapPlayer(event.getPlayer()).as(PlayerWrapper.class), (GameStore) event.getStore());
            }
        }
    }
}