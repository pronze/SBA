package io.github.pronze.sba.inventories;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.game.StoreType;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import io.github.pronze.sba.wrapper.PlayerWrapper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToItem;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.ConfigurateUtils;
import org.screamingsandals.lib.utils.Controllable;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.simpleinventories.SimpleInventoriesCore;
import org.screamingsandals.simpleinventories.builder.InventorySetBuilder;
import org.screamingsandals.simpleinventories.events.ItemRenderEvent;
import org.screamingsandals.simpleinventories.inventory.InventorySet;
import org.screamingsandals.simpleinventories.inventory.PlayerItemInfo;

import java.util.*;
import java.util.stream.Collectors;

@Service(dependsOn = {
        SimpleInventoriesCore.class,
})
public class SBAUpgradeStoreInventory extends AbstractStoreInventory {

    public static SBAStoreInventory getInstance() {
        return ServiceManager.get(SBAStoreInventory.class);
    }

    private final static List<String> upgradeProperties = List.of(
            "sharpness",
            "protection",
            "efficiency",
            "blindtrap",
            "minertrap",
            "healpool",
            "dragon"
    );

    public static List<Integer> sharpnessPrices = new ArrayList<>();
    public static List<Integer> protectionPrices = new ArrayList<>();
    public static List<Integer> efficiencyPrices = new ArrayList<>();

    private final Map<String, InventorySet> shopMap = new HashMap<>();

    public SBAUpgradeStoreInventory(Controllable controllable) {
        super("shops/upgradeShop.yml");
        controllable.postEnable(this::loadPrices);
    }

    private void loadPrices() {
        // make sure they contain at least one value
        sharpnessPrices.add(4);
        protectionPrices.add(4);
        efficiencyPrices.add(4);

        SBAConfig.getInstance().node("upgrades", "prices").childrenMap()
                .forEach((key, val) -> {
                    final var castedKey = ((String) key).toLowerCase();
                    final var value = val.getInt(4);
                    if (castedKey.startsWith("sharpness")) {
                        sharpnessPrices.add(value);
                    } else if (castedKey.startsWith("prot")) {
                        protectionPrices.add(value);
                    } else if (castedKey.startsWith("efficiency")) {
                        efficiencyPrices.add(value);
                    }
                });
        Logger.trace("Protection prices: {}", protectionPrices.stream().map(String::valueOf).collect(Collectors.toList()));
        Logger.trace("Efficiency prices: {}", efficiencyPrices.stream().map(String::valueOf).collect(Collectors.toList()));
        Logger.trace("Sharpness prices: {}", sharpnessPrices.stream().map(String::valueOf).collect(Collectors.toList()));
    }

    @Override
    public void onPostGenerateItem(ItemRenderEvent event) {
        ShopUtil.applyTeamUpgradeEnchantsToItem(event.getStack(), event, StoreType.UPGRADES);
    }

    @Override
    public void onPreGenerateItem(ItemRenderEvent event) {
        // do nothing here
    }

    @Override
    public Map.Entry<Boolean, Boolean> handlePurchase(Player player, ItemStack newItem, ItemStack materialItem, PlayerItemInfo itemInfo, ItemSpawnerType type) {
        boolean shouldSellStack = true;
        final var game = Main.getInstance().getGameOfPlayer(player);
        final var gameStorage = ArenaManager
                .getInstance()
                .get(game.getName())
                .orElseThrow()
                .getStorage();

        final var team = game.getTeamOfPlayer(player);
        final var wrappedPlayer = PlayerMapper.wrapPlayer(player);

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
                            var teamSharpnessLevel = gameStorage.getSharpnessLevel(team).orElseThrow();
                            var maxSharpnessLevel = SBAConfig.getInstance().node("upgrades", "limit", "Sharpness").getInt(1);

                            if (teamSharpnessLevel >= maxSharpnessLevel) {
                                shouldSellStack = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                                        .send(wrappedPlayer);
                            } else {
                                teamSharpnessLevel = teamSharpnessLevel + 1;
                                var ePrice = sharpnessPrices.get(teamSharpnessLevel);
                                materialItem = type.getStack(ePrice);

                                if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                                    gameStorage.setSharpnessLevel(team, teamSharpnessLevel);
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
                            var efficiencyLevel = gameStorage.getEfficiencyLevel(team).orElseThrow();
                            var maxEfficiencyLevel = SBAConfig.getInstance().node("upgrades", "limit", "Efficiency").getInt(2);

                            if (efficiencyLevel >= maxEfficiencyLevel) {
                                shouldSellStack = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                                        .send(wrappedPlayer);
                            } else {
                                efficiencyLevel = efficiencyLevel + 1;
                                var ePrice = efficiencyPrices.get(efficiencyLevel);
                                materialItem = type.getStack(ePrice);

                                if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                                    gameStorage.setEfficiencyLevel(team, efficiencyLevel);
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
                            if (gameStorage.areBlindTrapEnabled(team)) {
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

                                gameStorage.setPurchasedBlindTrap(team, true);
                                team.getConnectedPlayers().forEach(pl ->
                                        SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), blindnessTrapTitle, "", 20, 40, 20));
                            }
                            break;

                        case "minertrap":
                            if (gameStorage.areMinerTrapEnabled(team)) {
                                shouldSellStack = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.WAIT_FOR_TRAP)
                                        .send(wrappedPlayer);
                            } else {
                                final var minerTrapTitle = LanguageService
                                        .getInstance()
                                        .get(MessageKeys.MINER_TRAP_PURCHASED_TITLE)
                                        .toString();

                                gameStorage.setPurchasedMinerTrap(team, true);
                                team.getConnectedPlayers().forEach(pl ->
                                        SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), minerTrapTitle, "", 20, 40, 20));
                            }
                            break;

                        case "healpool":
                            shouldSellStack = false;
                            if (gameStorage.arePoolEnabled(team)) {
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

                                gameStorage.setPurchasedPool(team, true);
                                team.getConnectedPlayers().forEach(pl -> PlayerMapper.wrapPlayer(pl).sendMessage(purchaseHealPoolMessage));
                            }
                            break;

                        case "protection":
                            var teamProtectionLevel = gameStorage.getProtectionLevel(team).orElseThrow();
                            var maxProtectionLevel = SBAConfig.getInstance().node("upgrades", "limit", "Protection").getInt(4);

                            if (teamProtectionLevel >= maxProtectionLevel) {
                                shouldSellStack = false;
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                                        .send(wrappedPlayer);
                            } else {
                                teamProtectionLevel = teamProtectionLevel + 1;
                                var ePrice = protectionPrices.get(teamProtectionLevel);
                                materialItem = type.getStack(ePrice);

                                if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                                    gameStorage.setProtectionLevel(team, teamProtectionLevel);
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
                }
                var applyEvent = new BedwarsApplyPropertyToItem(game, player, newItem, propertyData);
                SBA.getPluginInstance().getServer().getPluginManager().callEvent(applyEvent);
                newItem = applyEvent.getStack();
            }
        }

        return Map.entry(shouldSellStack, false);
    }

    @Override
    public @NotNull InventorySetBuilder getInventorySetBuilder() {
        return SimpleInventoriesCore
                .builder()
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
                .variableToProperty("currency-changer", "currencyChanger");
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
