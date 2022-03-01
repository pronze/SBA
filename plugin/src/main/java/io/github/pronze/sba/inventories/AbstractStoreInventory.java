package io.github.pronze.sba.inventories;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.IStoreInventory;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.ShopUtil;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToBoughtItem;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToDisplayedItem;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.api.upgrades.Upgrade;
import org.screamingsandals.bedwars.api.upgrades.UpgradeRegistry;
import org.screamingsandals.bedwars.api.upgrades.UpgradeStorage;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.utils.ConfigurateUtils;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.simpleinventories.builder.InventorySetBuilder;
import org.screamingsandals.simpleinventories.events.ItemRenderEvent;
import org.screamingsandals.simpleinventories.events.OnTradeEvent;
import org.screamingsandals.simpleinventories.events.PreClickEvent;
import org.screamingsandals.simpleinventories.inventory.Include;
import org.screamingsandals.simpleinventories.inventory.InventorySet;
import org.screamingsandals.simpleinventories.inventory.PlayerItemInfo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public abstract class AbstractStoreInventory implements IStoreInventory, Listener {
    private final Map<String, InventorySet> shopMap = new HashMap<>();
    @NotNull
    private final String shopPaths;

    @OnPostEnable
    public void onPostEnable() {
        Arrays.stream(shopPaths.split(","))
                .forEach(path -> {
                    var shopFile = SBA
                            .getPluginInstance()
                            .getDataFolder()
                            .toPath()
                            .resolve("shops/"+path)
                            .toFile();

                    if (!shopFile.exists()) {
                        SBA.getInstance().saveResource("shops/"+path, false);
                    }
                });

        SBA.getInstance().registerListener(this);
        loadNewShop("default", null, true);
    }

    public Optional<InventorySet> getInventory(String key) {
        return Optional.ofNullable(shopMap.get(key));
    }

    @Override
    public void openForPlayer(@NotNull SBAPlayerWrapper player, @NotNull GameStore store) {
        try {
            var parent = true;
            parent = store.getUseParent();
            String fileName = store.getShopFile();

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
            Logger.error("[SBA] Your shop is invalid! Check it out or contact us on Discord. {}",ignored);
            player.sendMessage("[SBA] Your shop is invalid! Check it out or contact us on Discord.");
        }
    }

    @Override
    public void loadNewShop(@NotNull String name, @Nullable File file, boolean useParent) {
        final var inventorySet = getInventorySetBuilder()
                .genericShop(true)
                .genericShopPriceTypeRequired(true)
                .animationsEnabled(true)
                .call(categoryBuilder -> {
                    var pathStr = SBA.getPluginInstance().getDataFolder().getAbsolutePath();
                    pathStr = pathStr +  "/" +  (file != null ? file.getName() : shopPaths.split(",")[0]);
                    categoryBuilder.include(Include.of(Paths.get(pathStr)));
                })
                .preClick(this::onPreAction)
                .buy(this::onShopTransaction)
                .render(this::onGeneratingItem)
                .getInventorySet();

        try {
            inventorySet.getMainSubInventory().process();
        } catch (Exception ex) {
            Bukkit.getLogger().warning("Wrong shop.yml configuration!");
            Bukkit.getLogger().warning("Check validity of your YAML!");
            ex.printStackTrace();
            //loadDefault(inventorySet);
        }

        shopMap.put(name, inventorySet);
    }

    @SneakyThrows
    private void loadDefault(InventorySet inventorySet) {
        inventorySet.getMainSubInventory().dropContents();
        inventorySet.getMainSubInventory().getWaitingQueue().add(Include.of(Path.of(Objects.requireNonNull(SBA.class.getResource("/shops/" + shopPaths.split(",")[0])).toURI())));
        inventorySet.getMainSubInventory().process();
    }


    private void onShopTransaction(OnTradeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        handlePrePurchase(event);
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

    public void handlePrePurchase(OnTradeEvent event) {
        var player = event.getPlayer().as(Player.class);
        var game = Main.getInstance().getGameOfPlayer(player);

        var clickType = event.getClickType();
        var itemInfo = event.getItem();

        var price = event.getPrices().get(0);
        ItemSpawnerType type = Main.getSpawnerType(price.getCurrency().toLowerCase());

        var newItem = event.getStack().as(ItemStack.class);

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

            newItem = changeItemType.getStack();
        }

        var originalMaxStackSize = newItem.getType().getMaxStackSize();
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
                newItem.setAmount(amount);
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
                        .replace("%material%", type.getItemName())
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

                //temporary fix
                propertyData.putIfAbsent("name", property.getPropertyName());

                var applyEvent = new BedwarsApplyPropertyToBoughtItem(game, player, newItem, propertyData);
                Logger.trace("Calling event: {} for property: {}", applyEvent.getClass().getSimpleName(), property.getPropertyName());
                SBA.getPluginInstance().getServer().getPluginManager().callEvent(applyEvent);
                newItem = applyEvent.getStack();
            }
        }

        AtomicReference<ItemStack> newItemRef = new AtomicReference<ItemStack>(newItem);
        AtomicReference<Item> newMaterialItemRef = new AtomicReference<Item>(materialItem);
        final var result = handlePurchase(player, newItemRef, newMaterialItemRef, itemInfo, type);
        newItem = newItemRef.get();
        materialItem = newMaterialItemRef.get();
        final var shouldSellStack = result.getKey();
        final var shouldBuyStack = result.getValue();

        // purchase failed, player does not have enough resources to purchase
        if (!shouldBuyStack && !shouldSellStack) {
            if (!SBAConfig.getInstance().node("shop", "removePurchaseMessages").getBoolean()) {
                LanguageService
                        .getInstance()
                        .get(MessageKeys.CANNOT_BUY)
                        .replace("%material%", type.getItemName())
                        .send(event.getPlayer());
            }
            return;
        }
        
        if (shouldBuyStack) {
            buyStack(newItem, player);
        }

        if (shouldSellStack) {
            event.sellStack(materialItem);

            if (!SBAConfig.getInstance().node("shop", "removePurchaseMessages").getBoolean()) {
                LanguageService
                        .getInstance()
                        .get(MessageKeys.SHOP_PURCHASE_SUCCESS)
                        .replace("%item%", ShopUtil.getNameOrCustomNameOfItem(ItemFactory.build(newItem).orElseThrow()))
                        .replace("%material%", type.getItemName())
                        .send(event.getPlayer());
            }
            Sounds.playSound(player, player.getLocation(),
                    Main.getConfigurator().config.getString("sounds.item_buy.sound"), Sounds.ENTITY_ITEM_PICKUP, (float) Main.getConfigurator().config.getDouble("sounds.item_buy.volume"), (float) Main.getConfigurator().config.getDouble("sounds.item_buy.pitch"));
        }
    }

    private void buyStack(ItemStack newItem, Player player) {
        final HashMap<Integer, ItemStack> noFit = player.getInventory().addItem(newItem);
        if (!noFit.isEmpty()) {
            noFit.forEach((i, stack) -> player.getLocation().getWorld().dropItem(player.getLocation(), stack));
        }
    }

    private void onGeneratingItem(ItemRenderEvent event) {
        onPreGenerateItem(event);

        var itemInfo = event.getItem();
        var item = itemInfo.getStack();
        var player = event.getPlayer().as(Player.class);
        var game = Main.getInstance().getGameOfPlayer(player);

        if(itemInfo.getStack().getMaterial().is(Material.POTION))
        {
            var itemB= item.as(ItemStack.class);
            Logger.trace("{}",itemB);
        }
        var prices = itemInfo.getOriginal().getPrices();
        if (!prices.isEmpty()) {
            var priceObject = prices.get(0);
            var price = priceObject.getAmount();
            var type = Main.getSpawnerType(priceObject.getCurrency().toLowerCase());
            if (type == null) {
                return;
            }
            event.setStack(item=ShopUtil.setLore(item, itemInfo, String.valueOf(price), type, player));
        }
        event.setStack(item);

        itemInfo.getProperties().forEach(property -> {
            if (property.hasName()) {
                var converted = ConfigurateUtils.raw(property.getPropertyData());
                if (!(converted instanceof Map)) {
                    converted = ShopUtil.nullValuesAllowingMap("value", converted);
                }

                //noinspection unchecked
                var propertyData = (Map<String, Object>) converted;

                //temporary fix
                propertyData.putIfAbsent("name", property.getPropertyName());

                var applyEvent = new BedwarsApplyPropertyToDisplayedItem(game,
                        player, event.getStack().as(ItemStack.class), propertyData);
                Bukkit.getServer().getPluginManager().callEvent(applyEvent);

                event.setStack(ItemFactory.build(applyEvent.getStack()).orElse(event.getStack()));
            }
        });

        
        onPostGenerateItem(event);
    }

    public abstract void onPostGenerateItem(ItemRenderEvent event);

    public abstract void onPreGenerateItem(ItemRenderEvent event);

    public abstract Map.Entry<Boolean, Boolean> handlePurchase(Player player, AtomicReference<ItemStack> newItem, AtomicReference<Item> materialItem, PlayerItemInfo itemInfo, ItemSpawnerType type);

    @NotNull
    public abstract InventorySetBuilder getInventorySetBuilder();
}
