package pronze.hypixelify.inventories;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.OpenShopEvent;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.commands.DumpCommand;
import org.screamingsandals.bedwars.events.ApplyPropertyToBoughtItemEventImpl;
import org.screamingsandals.bedwars.events.ApplyPropertyToDisplayedItemEventImpl;
import org.screamingsandals.bedwars.events.OpenShopEventImpl;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.bedwars.lib.event.EventManager;
import org.screamingsandals.bedwars.lib.material.builder.ItemFactory;
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
import pronze.hypixelify.game.StoreWrapper;
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
import java.util.*;

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

        var itemInfo = event.getItem();

        var price = event.getPrices().get(0);
        ItemSpawnerType type = Main.getSpawnerType(price.getCurrency().toLowerCase());

        var newItem = event.getStack();

        var amount = newItem.getAmount();
        var priceAmount = price.getAmount();

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

        var shouldSell = true;

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
                Logger.trace("Found property: {} for item: {}", propertyName);

                if (upgradeProperties.contains(propertyName)) {
                    final var bukkitItem = newItem.as(ItemStack.class);

                    switch (propertyName) {
                        case "sharpness":
                            var teamSharpnessLevel = gameStorage.getSharpness(team.getName());
                            if (teamSharpnessLevel >= 4) {
                                shouldSell = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                                        .send(wrappedPlayer);
                            } else {
                                teamSharpnessLevel = teamSharpnessLevel + 1;
                                var ePrice = sharpnessPrices.get(teamSharpnessLevel);
                                materialItem = ItemFactory
                                        .build(type.getStack(ePrice))
                                        .orElse(materialItem);

                                if (player.getInventory().containsAtLeast(materialItem.as(ItemStack.class), materialItem.getAmount())) {
                                    gameStorage.setSharpness(team.getName(), teamSharpnessLevel);
                                    Integer finalTeamSharpnessLevel = teamSharpnessLevel;
                                    team.getConnectedPlayers().forEach(teamPlayer -> {
                                        LanguageService
                                                .getInstance()
                                                .get(MessageKeys.UGPRADE_TEAM_SHARPNESS)
                                                .replace("%player%", player.getName())
                                                .send(PlayerMapper.wrapPlayer(teamPlayer));

                                        Arrays.stream(teamPlayer.getInventory().getContents())
                                            .filter(Objects::nonNull)
                                            .forEach(item -> {
                                                if (item.getType().name().endsWith("SWORD")) {
                                                    item.addEnchantment(Enchantment.DAMAGE_ALL, finalTeamSharpnessLevel);
                                                }
                                            });
                                    });
                                }
                            }
                            break;

                        case "efficiency":
                            var efficiencyLevel = gameStorage.getEfficiency(team.getName());

                            if (efficiencyLevel >= 4) {
                                shouldSell = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                                        .send(wrappedPlayer);
                            } else {
                                //TODO:
                            }
                            break;
                        case "blindtrap":
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
                            var teamProtectionLevel = gameStorage.getProtection(team.getName());

                            if (teamProtectionLevel >= 4) {
                                shouldSell = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                                        .send(wrappedPlayer);
                            } else {
                                teamSharpnessLevel = teamProtectionLevel + 1;
                                var ePrice = protectionPrices.get(teamSharpnessLevel);
                                materialItem = ItemFactory
                                        .build(type.getStack(ePrice))
                                        .orElse(materialItem);

                                if (player.getInventory().containsAtLeast(materialItem.as(ItemStack.class), materialItem.getAmount())) {
                                    gameStorage.setProtection(team.getName(), teamSharpnessLevel);
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
                    var applyEvent = new ApplyPropertyToBoughtItemEventImpl(game, event.getPlayer().as(BedWarsPlayer.class), property.getPropertyName(), propertyData, newItem);
                    EventManager.fire(applyEvent);

                    newItem = applyEvent.getStack();
                }

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

                if (shouldSell) {
                    event.sellStack(materialItem);
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
        var game = PlayerManager
                .getInstance()
                .getGameOfPlayer(event.getPlayer())
                .orElseThrow();
        var prices = itemInfo.getOriginal().getPrices();
        if (!prices.isEmpty()) {
            var priceObject = prices.get(0);
            var price = priceObject.getAmount();
            var type = Main.getSpawnerType(priceObject.getCurrency().toLowerCase());
            if (type == null) {
                return;
            }
            ShopUtil.setLore(item, itemInfo, String.valueOf(price), type);
            ShopUtil.applyTeamUpgradeEnchantsToItem(item, event, StoreWrapper.Type.UPGRADES);

            itemInfo.getProperties().forEach(property -> {
                if (property.hasName()) {
                    var converted = ConfigurateUtils.raw(property.getPropertyData());
                    if (!(converted instanceof Map)) {
                        converted = DumpCommand.nullValuesAllowingMap("value", converted);
                    }

                    //noinspection unchecked
                    var applyEvent = new ApplyPropertyToDisplayedItemEventImpl(game,
                            PlayerManager.getInstance().getPlayer(player.getUniqueId()).orElseThrow(), property.getPropertyName(), (Map<String, Object>) converted, item);
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
                openForPlayer(event.getPlayer(), event.getGameStore());
            }
        }
    }
}
