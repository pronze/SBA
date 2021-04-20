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
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.OpenShopEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.api.upgrades.Upgrade;
import org.screamingsandals.bedwars.api.upgrades.UpgradeRegistry;
import org.screamingsandals.bedwars.api.upgrades.UpgradeStorage;
import org.screamingsandals.bedwars.commands.DumpCommand;
import org.screamingsandals.bedwars.config.MainConfig;
import org.screamingsandals.bedwars.events.ApplyPropertyToBoughtItemEventImpl;
import org.screamingsandals.bedwars.events.ApplyPropertyToDisplayedItemEventImpl;
import org.screamingsandals.bedwars.events.OpenShopEventImpl;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.bedwars.lib.event.EventManager;
import org.screamingsandals.bedwars.lib.event.OnEvent;
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
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import org.screamingsandals.bedwars.player.PlayerManager;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.game.IStoreInventory;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ShopUtil;
import pronze.lib.core.Core;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.annotations.OnInit;
import pronze.lib.core.auto.InitializationPriority;
import pronze.lib.core.utils.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//TODO: remove duplication between SBAStoreInventory and this class

@AutoInitialize(listener = true, initPriority = InitializationPriority.LOW)
public class TeamUpgradesStoreInventory implements IStoreInventory, Listener {
    private final static List<String> upgradeProperties = List.of(
            "sharpness",
            "protection",
            "blindtrap",
            "healpool",
            "dragon"
    );
    private final Map<String, InventorySet> shopMap = new HashMap<>();
    private final Map<Integer, Integer> sharpnessPrices = new HashMap<>();
    private final Map<Integer, Integer> protectionPrices = new HashMap<>();

    public static TeamUpgradesStoreInventory getInstance() {
        return Core.getObjectFromClass(TeamUpgradesStoreInventory.class);
    }

    /**
     * Method invoked on instantiation of this class.
     * Will save shop file to shops directory in case of it's absence and also load the default shops into the ShopMap.
     */
    @OnInit
    public void onCreation() {
        var upgradeShopFile = SBAHypixelify
                .getInstance()
                .getDataFolder()
                .toPath()
                .resolve("shops/upgradeShop.yml")
                .toFile();

        if (!upgradeShopFile.exists()) {
            SBAHypixelify
                    .getInstance()
                    .saveResource("shops/upgradeShop.yml", false);
        }

        loadNewShop("default", null, true);
        loadPrices();
        EventManager.getDefaultEventManager().register(OpenShopEventImpl.class, this::onBedWarsOpenShop);
    }

    private void loadPrices() {
        sharpnessPrices.put(0, SBAConfig.getInstance().node("upgrades", "prices", "Sharpness-I").getInt(4));
        sharpnessPrices.put(1, SBAConfig.getInstance().node("upgrades", "prices", "Sharpness-I").getInt(4));
        sharpnessPrices.put(2, SBAConfig.getInstance().node("upgrades", "prices", "Sharpness-II").getInt(8));
        sharpnessPrices.put(3, SBAConfig.getInstance().node("upgrades", "prices", "Sharpness-III").getInt(12));
        sharpnessPrices.put(4, SBAConfig.getInstance().node("upgrades", "prices", "Sharpness-IV").getInt(16));

        protectionPrices.put(0, SBAConfig.getInstance().node("upgrades", "prices", "Prot-I").getInt(4));
        protectionPrices.put(1, SBAConfig.getInstance().node("upgrades", "prices", "Prot-I").getInt(4));
        protectionPrices.put(2, SBAConfig.getInstance().node("upgrades", "prices", "Prot-II").getInt(8));
        protectionPrices.put(3, SBAConfig.getInstance().node("upgrades", "prices", "Prot-III").getInt(12));
        protectionPrices.put(4, SBAConfig.getInstance().node("upgrades", "prices", "Prot-IV").getInt(16));
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

    @Override
    public void loadNewShop(String name, File file, boolean useParent) {
        var inventorySet = SimpleInventoriesCore
                .builder()
                .genericShop(true)
                .genericShopPriceTypeRequired(true)
                .animationsEnabled(true)
                .categoryOptions(localOptionsBuilder ->
                        localOptionsBuilder
                                .backItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "upgrade-shop", "shopback"), "BARRIER"), itemBuilder ->
                                        itemBuilder.name(LanguageService.getInstance().get(MessageKeys.SHOP_PAGE_BACK).toComponent())
                                )
                                .pageBackItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "upgrade-shop", "pageback"), "ARROW"), itemBuilder ->
                                        itemBuilder.name(LanguageService.getInstance().get(MessageKeys.SHOP_PAGE_BACK).toComponent())
                                )
                                .pageForwardItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "upgrade-shop", "pageforward"), "BARRIER"), itemBuilder ->
                                        itemBuilder.name(LanguageService.getInstance().get(MessageKeys.SHOP_PAGE_FORWARD).toComponent())
                                )
                                .cosmeticItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "upgrade-shop", "shopcosmetic"), "AIR"))
                                .rows(SBAConfig.getInstance().node("shop", "upgrade-shop", "rows").getInt(4))
                                .renderActualRows(SBAConfig.getInstance().node("shop", "upgrade-shop", "render-actual-rows").getInt(6))
                                .renderOffset(SBAConfig.getInstance().node("shop", "upgrade-shop", "render-offset").getInt(9))
                                .renderHeaderStart(SBAConfig.getInstance().node("shop", "upgrade-shop", "render-header-start").getInt(0))
                                .renderFooterStart(SBAConfig.getInstance().node("shop", "upgrade-shop", "render-footer-start").getInt(45))
                                .itemsOnRow(SBAConfig.getInstance().node("shop", "upgrade-shop", "items-on-row").getInt(9))
                                .showPageNumber(SBAConfig.getInstance().node("shop", "upgrade-shop", "show-page-numbers").getBoolean(true))
                                .inventoryType(SBAConfig.getInstance().node("shop", "upgrade-shop", "inventory-type").getString("CHEST"))
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
                    var pathStr = SBAHypixelify.getInstance().getDataFolder().getAbsolutePath();
                    pathStr = pathStr + "/shops/" + (file != null ? file.getName() : "upgradeShop.yml");
                    categoryBuilder.include(Include.of(Paths.get(pathStr)));
                })
                .getInventorySet();

        try {
            inventorySet.getMainSubInventory().process();
        } catch (Exception ex) {
            Bukkit.getLogger().warning("Wrong upgradeShop.yml configuration!");
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

        var shouldSell = true;
        var shouldBuyStack = true;
        final var wrappedPlayer = event.getPlayer();
        final var gameStorage = SBAHypixelify
                .getInstance()
                .getGameStorage(game)
                .orElseThrow();
        final var team = game.getTeamOfPlayer(player);

        for (var property : itemInfo.getProperties()) {
            if (property.hasName()) {
                var converted = ConfigurateUtils.raw(property.getPropertyData());
                if (!(converted instanceof Map)) {
                    converted = DumpCommand.nullValuesAllowingMap("value", converted);
                }
                //noinspection unchecked
                var propertyData = (Map<String, Object>) converted;
                final var propertyName = property.getPropertyName().toLowerCase();
                if (upgradeProperties.contains(propertyName)) {
                    final var bukkitItem = newItem.as(ItemStack.class);

                    switch (propertyName) {
                        case "sharpness":
                            shouldBuyStack = false;
                            if (!ShopUtil.addEnchantsToTeamTools(player, bukkitItem, "SWORD", Enchantment.DAMAGE_ALL)) {
                                shouldSell = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                                        .send(wrappedPlayer);
                            } else {
                                var level = bukkitItem.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                                var ePrice = sharpnessPrices.get(level);
                                materialItem = ItemFactory
                                        .build(type.getStack(ePrice))
                                        .orElse(materialItem);

                                if (player.getInventory().containsAtLeast(materialItem.as(ItemStack.class), materialItem.getAmount())) {
                                    gameStorage.setSharpness(team.getName(), level);
                                }
                            }
                            break;

                        case "efficiency":
                            shouldBuyStack = false;
                            if (!ShopUtil.addEnchantsToTeamTools(player, bukkitItem, "PICKAXE", Enchantment.DIG_SPEED)) {
                                shouldSell = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                                        .send(wrappedPlayer);
                            }
                            break;
                        case "blindtrap":
                            shouldBuyStack = false;
                            if (gameStorage.isTrapEnabled(team)) {
                                shouldSell = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.WAIT_FOR_TRAP)
                                        .send(wrappedPlayer);
                            } else {
                                final var blindnessTrapTitle = LanguageService
                                        .getInstance()
                                        .get(MessageKeys.BLINDNESS_TRAP_PURCHASED_TITLE)
                                        .toString();

                                gameStorage.setTrap(team, true);
                                team.getConnectedPlayers().forEach(pl ->
                                        SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), blindnessTrapTitle, "", 20, 40, 20));
                            }
                            break;

                        case "healpool":
                            shouldBuyStack = false;
                            shouldSell = false;
                            if (gameStorage.isPoolEnabled(team)) {
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.WAIT_FOR_TRAP)
                                        .send(wrappedPlayer);
                            } else {
                                var purchaseHealPoolMessage = LanguageService
                                        .getInstance()
                                        .get(MessageKeys.PURCHASED_HEAL_POOL_MESSAGE)
                                        .replace("%player%", player.getName())
                                        .toComponent();

                                gameStorage.setPool(team, true);
                                team.getConnectedPlayers().forEach(pl -> PlayerMapper.wrapPlayer(pl).sendMessage(purchaseHealPoolMessage));
                            }
                            break;

                        case "protection":
                            shouldBuyStack = false;
                            if (gameStorage.getProtection(team.getName()) >= 4) {
                                shouldSell = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                                        .send(wrappedPlayer);
                            } else {
                                var level = bukkitItem.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
                                var ePrice = protectionPrices.get(level);
                                materialItem = ItemFactory
                                        .build(type.getStack(ePrice))
                                        .orElse(materialItem);

                                if (player.getInventory().containsAtLeast(materialItem.as(ItemStack.class), materialItem.getAmount())) {
                                    gameStorage.setProtection(team.getName(), level);
                                }

                                ShopUtil.addEnchantsToPlayerArmor(player, bukkitItem);

                                var upgradeMessage = LanguageService
                                        .getInstance()
                                        .get(MessageKeys.UPGRADE_TEAM_PROTECTION)
                                        .replace("%player%", player.getName())
                                        .toComponent();

                                team.getConnectedPlayers().forEach(teamPlayer -> {
                                    ShopUtil.addEnchantsToPlayerArmor(teamPlayer, bukkitItem);
                                    PlayerMapper.wrapPlayer(teamPlayer).sendMessage(upgradeMessage);
                                });
                            }
                            break;
                    }
                } else {
                    var applyEvent = new ApplyPropertyToBoughtItemEventImpl(game, event.getPlayer().as(BedWarsPlayer.class),  property.getPropertyName(), propertyData, newItem);
                    EventManager.fire(applyEvent);

                    newItem = applyEvent.getStack();
                }

                final var typeName = newItem.getMaterial().getPlatformName();

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

                if (!event.hasPlayerInInventory(materialItem)) {
                    if (!SBAConfig.getInstance().node("shop", "removePurchaseMessages").getBoolean()) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.SHOP_PURCHASE_SUCCESS)
                                .replace("<item>", amount + "x " + ShopUtil.getNameOrCustomNameOfItem(newItem))
                                .replace("<material>", priceAmount + " " + type.getItemName())
                                .send(event.getPlayer());
                    }
                }

                if (shouldSell) {
                    event.sellStack(materialItem);
                }

                if (shouldBuyStack) {
                    List<Item> notFit = event.buyStack(newItem);
                    if (!notFit.isEmpty()) {
                        notFit.forEach(stack -> player.getLocation().getWorld().dropItem(player.getLocation(), stack.as(ItemStack.class)));
                    }
                }
            }
        }
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
        var game = PlayerManager.getInstance().getGameOfPlayer(event.getPlayer()).orElseThrow();
        var prices = itemInfo.getOriginal().getPrices();
        if (!prices.isEmpty()) {
            var priceObject = prices.get(0);
            var price = priceObject.getAmount();
            var type = Main.getSpawnerType(priceObject.getCurrency().toLowerCase());
            if (type == null) {
                return;
            }
            ShopUtil.setLore(item, itemInfo, String.valueOf(price), type);
            ShopUtil.applyTeamUpgradeEnchantsToItem(item, event);

            itemInfo.getProperties().forEach(property -> {
                if (property.hasName()) {
                    var converted = ConfigurateUtils.raw(property.getPropertyData());
                    if (!(converted instanceof Map)) {
                        converted = DumpCommand.nullValuesAllowingMap("value", converted);
                    }

                    //noinspection unchecked
                    var applyEvent = new ApplyPropertyToDisplayedItemEventImpl(game,
                            event.getPlayer().as(BedWarsPlayer.class),  property.getPropertyName(), (Map<String, Object>) converted, item);
                    EventManager.fire(applyEvent);

                    event.setStack(ItemFactory.build(applyEvent.getStack()).orElse(item));
                }
            });
        }
    }

    @SneakyThrows
    private void loadDefault(InventorySet inventorySet) {
        inventorySet.getMainSubInventory().dropContents();
        inventorySet.getMainSubInventory().getWaitingQueue().add(Include.of(Path.of(TeamUpgradesStoreInventory.class.getResource("/shops/upgradeShop.yml").toURI())));
        inventorySet.getMainSubInventory().process();
    }

    public void onBedWarsOpenShop(OpenShopEventImpl event) {
        final var shopFile = event.getGameStore().getShopFile();
        if (shopFile != null && shopFile.equalsIgnoreCase("upgradeShop.yml")) {
            if (SBAConfig.getInstance().node("shop", "upgrade-shop", "enabled").getBoolean()) {
                event.setResult(OpenShopEvent.Result.DISALLOW_UNKNOWN);
                Logger.trace("Player: {} has opened team upgrades store!", event.getPlayer().getName());
                openForPlayer(PlayerMapper.wrapPlayer(event.getPlayer()), event.getGameStore());
            }
        }
    }
}
