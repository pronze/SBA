package io.github.pronze.sba.inventories;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.IStoreInventory;
import io.github.pronze.sba.game.StoreType;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import io.github.pronze.sba.wrapper.PlayerWrapper;
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
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.ConfigurateUtils;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.simpleinventories.SimpleInventoriesCore;
import org.screamingsandals.simpleinventories.events.ItemRenderEvent;
import org.screamingsandals.simpleinventories.events.OnTradeEvent;
import org.screamingsandals.simpleinventories.events.PreClickEvent;
import org.screamingsandals.simpleinventories.inventory.Include;
import org.screamingsandals.simpleinventories.inventory.InventorySet;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service(dependsOn = {
        SimpleInventoriesCore.class,
})
public class SBAUpgradeStoreInventory implements IStoreInventory, Listener {
    private final static List<String> upgradeProperties = List.of(
            "sharpness",
            "protection",
            "efficiency",
            "blindtrap",
            "healpool",
            "dragon"
    );

    private final Map<String, InventorySet> shopMap = new HashMap<>();

    // change these to lists instead.
    private final Map<Integer, Integer> sharpnessPrices = new HashMap<>();
    private final Map<Integer, Integer> protectionPrices = new HashMap<>();
    private final Map<Integer, Integer> efficiencyPrices = new HashMap<>();

    public static SBAStoreInventory getInstance() {
        return ServiceManager.get(SBAStoreInventory.class);
    }

    @OnPostEnable
    public void onPostEnable() {
        var shopFile = SBA
                .getPluginInstance()
                .getDataFolder()
                .toPath()
                .resolve("shops/upgradeShop.yml")
                .toFile();

        if (!shopFile.exists()) {
            SBA.getInstance()
                    .saveResource("shops/upgradeShop.yml", false);
            loadNewShop("default", null, true);
        }
        SBA.getInstance().registerListener(this);
        loadPrices();
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

        efficiencyPrices.put(0, SBAConfig.getInstance().node("upgrades", "prices", "Efficiency-I").getInt(4));
        efficiencyPrices.put(1, SBAConfig.getInstance().node("upgrades", "prices", "Efficiency-I").getInt(4));
        efficiencyPrices.put(2, SBAConfig.getInstance().node("upgrades", "prices", "Efficiency-II").getInt(8));
        efficiencyPrices.put(3, SBAConfig.getInstance().node("upgrades", "prices", "Efficiency-III").getInt(12));
        efficiencyPrices.put(4, SBAConfig.getInstance().node("upgrades", "prices", "Efficiency-IV").getInt(16));    }


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
            player.sendMessage("[SBA] Your upgradeShop.yml is invalid! Check it out or contact us on Discord.");
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

        ShopUtil.applyTeamUpgradeEnchantsToItem(item, event, StoreType.UPGRADES);

        itemInfo.getProperties().forEach(property -> {
            if (property.hasName()) {
                var converted = ConfigurateUtils.raw(property.getPropertyData());
                if (!(converted instanceof Map)) {
                    converted = ShopUtil.nullValuesAllowingMap("value", converted);
                }

                if (((Map<?, ?>) converted).get("name") == null) {
                    return;
                }

                //noinspection unchecked
                var applyEvent = new BedwarsApplyPropertyToDisplayedItem(game,
                        player, item.as(ItemStack.class), (Map<String, Object>) converted);
                Bukkit.getServer().getPluginManager().callEvent(applyEvent);

                event.setStack(ItemFactory.build(applyEvent.getStack()).orElse(item));
            }
        });
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
                        .get(MessageKeys.CANNOT_BUY)
                        .replace("%price%", type.getItemName())
                        .send(event.getPlayer());
            }
            return;
        }

        final var gameStorage = SBA
                .getInstance()
                .getGameStorage(game)
                .orElseThrow();

        final var team = game.getTeamOfPlayer(player);

        var shouldSellStack = true;
        final var wrappedPlayer = event.getPlayer();

        for (var property : itemInfo.getProperties()) {
            if (property.hasName()) {
                final var propertyName = property.getPropertyName().toLowerCase();
                var converted = ConfigurateUtils.raw(property.getPropertyData());

                if (!(converted instanceof Map)) {
                    converted = ShopUtil.nullValuesAllowingMap("value", converted);
                }
                //noinspection unchecked
                var propertyData = (Map<String, Object>) converted;

                //temporary fix
                propertyData.putIfAbsent("name", propertyName);

                if (upgradeProperties.contains(propertyName)) {
                    switch (propertyName) {
                        case "sharpness":
                            var teamSharpnessLevel = gameStorage.getSharpness(team.getName());
                            if (teamSharpnessLevel >= 4) {
                                shouldSellStack = false;
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
                                shouldSellStack = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                                        .send(wrappedPlayer);
                            } else {
                                efficiencyLevel = efficiencyLevel + 1;
                                var ePrice = efficiencyPrices.get(efficiencyLevel);
                                materialItem = ItemFactory
                                        .build(type.getStack(ePrice))
                                        .orElse(materialItem);

                                if (player.getInventory().containsAtLeast(materialItem.as(ItemStack.class), materialItem.getAmount())) {
                                    gameStorage.setEfficiency(team, efficiencyLevel);
                                    Integer finalTeamEfficiencyLevel = efficiencyLevel;
                                    team.getConnectedPlayers().forEach(teamPlayer -> {
                                        LanguageService
                                                .getInstance()
                                                .get(MessageKeys.UPGRADE_TEAM_EFFICIENCY)
                                                .replace("%player%", player.getName())
                                                .send(PlayerMapper.wrapPlayer(teamPlayer));

                                        Arrays.stream(teamPlayer.getInventory().getContents())
                                                .filter(Objects::nonNull)
                                                .forEach(item -> {
                                                    if (item.getType().name().endsWith("PICKAXE")) {
                                                        item.addEnchantment(Enchantment.DIG_SPEED, finalTeamEfficiencyLevel);
                                                    }
                                                });
                                    });
                                }
                            }
                            break;
                        case "blindtrap":
                            if (gameStorage.isTrapEnabled(team)) {
                                shouldSellStack = false;
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
                            shouldSellStack = false;
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
                                shouldSellStack = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                                        .send(wrappedPlayer);
                            } else {
                                teamProtectionLevel = teamProtectionLevel + 1;
                                var ePrice = protectionPrices.get(teamProtectionLevel);
                                materialItem = ItemFactory
                                        .build(type.getStack(ePrice))
                                        .orElse(materialItem);

                                if (player.getInventory().containsAtLeast(materialItem.as(ItemStack.class), materialItem.getAmount())) {
                                    gameStorage.setProtection(team.getName(), teamProtectionLevel);
                                    ShopUtil.addEnchantsToPlayerArmor(player, teamProtectionLevel);

                                    var upgradeMessage = LanguageService
                                            .getInstance()
                                            .get(MessageKeys.UPGRADE_TEAM_PROTECTION)
                                            .replace("%player%", player.getName())
                                            .toComponent();

                                    final var finalTeamProtectionLevel = teamProtectionLevel;
                                    team.getConnectedPlayers().forEach(teamPlayer -> {
                                        ShopUtil.addEnchantsToPlayerArmor(teamPlayer, finalTeamProtectionLevel);
                                        PlayerMapper.wrapPlayer(teamPlayer).sendMessage(upgradeMessage);
                                    });
                                }
                            }
                            break;
                    }

                    var applyEvent = new BedwarsApplyPropertyToItem(game, player, newItem.as(ItemStack.class), propertyData);
                    SBA.getPluginInstance().getServer().getPluginManager().callEvent(applyEvent);
                    newItem = ItemFactory
                            .build(applyEvent.getStack())
                            .orElse(newItem);
                }
            }
        }

        // purchase failed, player does not have enough resources to purchase
        if (!event.hasPlayerInInventory(materialItem)) {
            if (!SBAConfig.getInstance().node("shop", "removePurchaseMessages").getBoolean()) {
                LanguageService
                        .getInstance()
                        .get(MessageKeys.CANNOT_BUY)
                        .replace("%price%", type.getItemName())
                        .send(event.getPlayer());
            }
            return;
        }

        if (shouldSellStack) {
            event.sellStack(materialItem);

            if (!SBAConfig.getInstance().node("shop", "removePurchaseMessages").getBoolean()) {
                LanguageService
                        .getInstance()
                        .get(MessageKeys.SHOP_PURCHASE_SUCCESS)
                        .replace("<item>", ShopUtil.getNameOrCustomNameOfItem(newItem))
                        .replace("<material>", priceAmount + " " + type.getItemName())
                        .send(event.getPlayer());
            }
        }
    }

    @SneakyThrows
    private void loadDefault(InventorySet inventorySet) {
        inventorySet.getMainSubInventory().dropContents();
        inventorySet.getMainSubInventory().getWaitingQueue().add(Include.of(Path.of(SBAStoreInventory.class.getResource("/shops/upgradeShop.yml").toURI())));
        inventorySet.getMainSubInventory().process();
    }

    @EventHandler
    public void onBedWarsOpenShop(BedwarsOpenShopEvent event) {
        final var shopFile = event.getStore().getShopFile();
        if ((shopFile != null && shopFile.equalsIgnoreCase("upgradeShop.yml"))) {
            if (SBAConfig.getInstance().node("shop", "upgrade-shop", "enabled").getBoolean()) {
                event.setResult(BedwarsOpenShopEvent.Result.DISALLOW_UNKNOWN);
                Logger.trace("Player: {} has opened upgrades store!", event.getPlayer().getName());
                openForPlayer(PlayerMapper.wrapPlayer(event.getPlayer()).as(PlayerWrapper.class), (GameStore) event.getStore());
            }
        }
    }
}
