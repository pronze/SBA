package pronze.hypixelify.store;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.api.upgrades.Upgrade;
import org.screamingsandals.bedwars.api.upgrades.UpgradeRegistry;
import org.screamingsandals.bedwars.api.upgrades.UpgradeStorage;
import org.screamingsandals.bedwars.commands.DumpCommand;
import org.screamingsandals.bedwars.config.MainConfig;
import org.screamingsandals.bedwars.game.CurrentTeam;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.game.GamePlayer;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.bedwars.lib.ext.configurate.ConfigurationNode;
import org.screamingsandals.bedwars.lib.material.Item;
import org.screamingsandals.bedwars.lib.material.builder.ItemFactory;
import org.screamingsandals.bedwars.lib.sgui.builder.CategoryBuilder;
import org.screamingsandals.bedwars.lib.sgui.builder.InventorySetBuilder;
import org.screamingsandals.bedwars.lib.sgui.events.ItemRenderEvent;
import org.screamingsandals.bedwars.lib.sgui.events.OnTradeEvent;
import org.screamingsandals.bedwars.lib.sgui.events.PreClickEvent;
import org.screamingsandals.bedwars.lib.sgui.inventory.Include;
import org.screamingsandals.bedwars.lib.sgui.inventory.PlayerItemInfo;
import org.screamingsandals.bedwars.lib.sgui.inventory.Property;
import org.screamingsandals.bedwars.lib.utils.AdventureHelper;
import org.screamingsandals.bedwars.lib.utils.ConfigurateUtils;
import org.screamingsandals.bedwars.utils.Sounds;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAStoreOpenEvent;
import pronze.hypixelify.api.events.TeamUpgradePurchaseEvent;
import pronze.hypixelify.listener.TeamUpgradeListener;
import pronze.hypixelify.store.AbstractStore;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.ShopUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.screamingsandals.bedwars.lib.lang.I.i18nc;
import static pronze.hypixelify.lib.lang.I.i18n;

public class SBAGameStore extends AbstractStore {

    private final static List<String> upgradeProperties = Arrays.asList(
            "sharpness",
            "protection",
            "blindtrap",
            "healpool",
            "dragon"
    );

    public SBAGameStore() { super(); }

    @Override
    public void onGeneratingItem(ItemRenderEvent event) {
        var itemInfo = event.getItem();
        var item = itemInfo.getStack();
        var player = event.getPlayer().as(Player.class);
        var game = Main.getPlayerGameProfile(player).getGame();
        var prices = itemInfo.getOriginal().getPrices();
        if (!prices.isEmpty()) {
            var priceObject = prices.get(0);
            var price = priceObject.getAmount();
            var type = Main.getSpawnerType(priceObject.getCurrency().toLowerCase());
            if (type == null) {
                return;
            }
            setLore(item, itemInfo, String.valueOf(price), type);
        }

        final var optionalStorage = SBAHypixelify.getInstance().getGameStorage(game);
        if (optionalStorage.isPresent()) {
            final var storage = optionalStorage.get();
            final var bukkitItemStack = item.as(ItemStack.class);
            final var typeName = bukkitItemStack.getType().name();
            final var runningTeam = game.getTeamOfPlayer(player);

            if (typeName.endsWith("SWORD")) {
                int sharpness = storage.getSharpness(runningTeam.getName());
                bukkitItemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpness);
            } else if (typeName.endsWith("BOOTS")) {
                int protection = storage.getProtection(runningTeam.getName());
                bukkitItemStack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protection);
            } else if (typeName.endsWith("PICKAXE")) {
                final int efficiency = storage.getEfficiency(runningTeam.getName());
                bukkitItemStack.addUnsafeEnchantment(Enchantment.DIG_SPEED, efficiency);
            }
            item = ItemFactory.build(bukkitItemStack).orElse(item);
        }

        Item finalItem = item;
        itemInfo.getProperties().stream()
                .filter(Property::hasName).forEach(property -> {
                var converted = ConfigurateUtils.raw(property.getPropertyData());
                if (!(converted instanceof Map)) {
                    converted = DumpCommand.nullValuesAllowingMap("value", converted);
                }
                //noinspection unchecked
                var applyEvent = new BedwarsApplyPropertyToDisplayedItem(game,
                        player, finalItem.as(ItemStack.class), property.getPropertyName(), (Map<String, Object>) converted);
                SBAHypixelify.getInstance().getServer().getPluginManager().callEvent(applyEvent);
                event.setStack(ItemFactory.build(applyEvent.getStack()).orElse(finalItem));
        });
    }

    public Item setLore(Item item,
                        PlayerItemInfo itemInfo,
                        String price,
                        ItemSpawnerType type) {
        var enabled = itemInfo.getFirstPropertyByName("generateLore")
                .map(property -> property.getPropertyData().getBoolean())
                .orElseGet(() -> MainConfig.getInstance().node("lore", "generate-automatically").getBoolean(true));

        if (enabled) {
            var loreText = itemInfo.getFirstPropertyByName("generatedLoreText")
                    .map(property -> property.getPropertyData().childrenList().stream().map(ConfigurationNode::getString))
                    .orElseGet(() -> MainConfig.getInstance().node("lore", "text").childrenList().stream().map(ConfigurationNode::getString))
                    .map(s -> s
                            .replaceAll("%price%", price)
                            .replaceAll("%resource%", type.getItemName())
                            .replaceAll("%amount%", Integer.toString(itemInfo.getStack().getAmount())))
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .map(AdventureHelper::toComponent)
                    .collect(Collectors.toList());

            item.getLore().addAll(loreText);
        }
        return item;
    }

    @Override
    public void onPreAction(PreClickEvent event) {
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

    @Override
    public void onShopTransaction(OnTradeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getItem().getFirstPropertyByName("upgrade").isPresent()) {
            handleUpgrade(event);
        } else {
            handleBuy(event);
        }
    }

    @Override
    public void categoryBuilderCall(CategoryBuilder builder, File file, String name) {
        try {
            Logger.trace("File: {}", file != null ? file.getName() : "null");
            Logger.trace("Name: {}", name != null ? name : "null");
            var pathStr = SBAHypixelify.getInstance().getDataFolder().getAbsolutePath();
            Logger.trace("Path str: {}", pathStr);
            pathStr = pathStr + "/shops/" + (file != null ? file.getName() : "shop.yml");
            Logger.trace("Path str: {}", pathStr);
            builder.include(Include.of(Paths.get(pathStr)));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @EventHandler
    public void onApplyPropertyToBoughtItem(BedwarsApplyPropertyToItem event) {
        if (event.getPropertyName().equalsIgnoreCase("applycolorbyteam")
                || event.getPropertyName().equalsIgnoreCase("transform::applycolorbyteam")) {
            Player player = event.getPlayer();
            CurrentTeam team = (CurrentTeam) event.getGame().getTeamOfPlayer(player);

            if (MainConfig.getInstance().node("automatic-coloring-in-shop").getBoolean()) {
                event.setStack(Main.applyColor(team.teamInfo.color, event.getStack()));
            }
        }
    }

    @EventHandler
    public void onShopOpen(BedwarsOpenShopEvent event) {
        if (SBAHypixelify.getConfigurator().config.getBoolean("store.replace-store-with-hypixelstore", true)) {
            event.setResult(BedwarsOpenShopEvent.Result.DISALLOW_THIRD_PARTY_SHOP);
            final var player = event.getPlayer();
            final var store = event.getStore();
            final var shopOpenEvent = new SBAStoreOpenEvent(player, store);
            SBAHypixelify.getInstance().getServer().getPluginManager().callEvent(shopOpenEvent);
            if (shopOpenEvent.isCancelled()) {
                return;
            }
            show(event.getPlayer(), (GameStore) event.getStore());
        }

    }

    private Optional<Item> upgradeItem(OnTradeEvent event, String propertyName) {
        final var player = event.getPlayer().as(Player.class);
        final var stack = event.getStack().as(ItemStack.class);
        final var game = Main.getPlayerGameProfile(player).getGame();
        final var itemInfo = event.getItem();
        final var team = game.getTeamOfPlayer(player);
        final var optionalGameStorage = SBAHypixelify.getInstance().getGameStorage(game);
        String price = null;

        if (optionalGameStorage.isEmpty()) {
            Logger.trace("Game storage empty at ApplyPropertyToItemEvent");
            return Optional.empty();
        }

        final var gameStorage = optionalGameStorage.get();

        if (propertyName.equalsIgnoreCase("sharpness")
                || propertyName.equalsIgnoreCase("protection")) {
            final var isSharp = propertyName.equalsIgnoreCase("sharpness");
            final var enchant = isSharp ? Enchantment.DAMAGE_ALL :
                    Enchantment.PROTECTION_ENVIRONMENTAL;

            final var level = isSharp ? gameStorage.getSharpness(team.getName()) + 1 :
                    gameStorage.getProtection(team.getName()) + 1;

            if (level >= 5) {
                stack.removeEnchantment(enchant);
                stack.setLore(
                        SBAHypixelify
                        .getConfigurator()
                        .getStringList("message.maximum-enchant-lore")
                );
                stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            } else {
                stack.addEnchantment(enchant, level);
                price = Integer.toString(TeamUpgradeListener.prices.get(level));
            }
        }

        if (stack.hasItemMeta()) {
            final var lores = stack.getItemMeta().getLore();
            if (lores != null) {
                for (var lore : lores) {
                    if (lore.contains("Maximum Enchant")) return Optional.empty();
                }
            }
        }

        if (price != null) {
            return Optional.of(setLore(ItemFactory.build(stack).orElse(event.getStack()),
                    itemInfo,
                    price,
                    Main.getSpawnerType(event.getPrices().get(0).getCurrency().toLowerCase()))
            );
        }
        return Optional.empty();
    }

    private void handleBuy(OnTradeEvent event) {
        var player = event.getPlayer().as(Player.class);
        var game = Main.getPlayerGameProfile(player).getGame();
        var clickType = event.getClickType();
        var itemInfo = event.getItem();

        // TODO: multi-price feature
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

            newItem = ItemFactory.build(
                    changeItemType.getStack()
            ).orElse(newItem);
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

        var materialItem = ItemFactory.build(type.getStack(priceAmount)).orElseThrow();
        if (event.hasPlayerInInventory(materialItem)) {
            for (var property : itemInfo.getProperties()) {
                var converted = ConfigurateUtils.raw(property.getPropertyData());
                if (!(converted instanceof Map)) {
                    converted = DumpCommand.nullValuesAllowingMap("value", converted);
                }
                //noinspection unchecked
                var propertyData = (Map<String, Object>) converted;
                if (property.hasName()) {
                    //Upgrade property
                    if (upgradeProperties.contains(property.getPropertyName())) {
                        final var propertyName = property.getPropertyName().toLowerCase();
                        Logger.trace(
                                "Found property: {} for ItemStack: {}",
                                propertyName,
                                event.getStack().getDisplayName()
                        );

                        final var itemOptional = upgradeItem(event, propertyName);
                        newItem = itemOptional.orElse(newItem);

                        final var teamUpgradeEvent = new TeamUpgradePurchaseEvent(
                                player,
                                newItem.as(ItemStack.class),
                                propertyName,
                                game.getTeamOfPlayer(player),
                                game,
                                type
                        );
                        Logger.trace("Calling event: {}", teamUpgradeEvent.getClass().getSimpleName());
                        SBAHypixelify.getInstance().getServer().getPluginManager().callEvent(teamUpgradeEvent);
                        if (teamUpgradeEvent.isCancelled()) {
                            return;
                        }

                        materialItem = ItemFactory.build(
                                type.getStack(Integer.parseInt(teamUpgradeEvent.getPrice()))
                        ).orElse(materialItem);

                        //since we are  setting the price to a different one on upgrade, we do the check again
                        if (!event.hasPlayerInInventory(materialItem)
                                && !MainConfig.getInstance().node("removePurchaseMessages").getBoolean(false)) {
                            player.sendMessage(i18n("cannot-buy"));
                            return;
                        }

                        event.sellStack(materialItem);
                        if (!MainConfig.getInstance().node("removePurchaseMessages").getBoolean(false)) {
                            player.sendMessage("§aYou purchased §e" + getNameOrCustomNameOfItem(newItem));
                        }
                        Sounds.playSound(player, player.getLocation(),
                                MainConfig.getInstance().node("sounds", "on_item_buy").getString(),
                                Sounds.ENTITY_ITEM_PICKUP, 1, 1);

                        return;
                    } else {
                        var applyEvent = new BedwarsApplyPropertyToBoughtItem(game, player,
                                newItem.as(ItemStack.class), property.getPropertyName(), propertyData);
                        SBAHypixelify.getInstance().getServer().getPluginManager().callEvent(applyEvent);

                        newItem = ItemFactory.build(applyEvent.getStack()).orElse(newItem);
                    }
                }
            }

            final var typeName = newItem.getMaterial().as(Material.class).name();
            final var optionalStorage = SBAHypixelify.getInstance().getGameStorage(game);
            var shouldSell = true;

            if (optionalStorage.isPresent()) {
                final var gameStorage = optionalStorage.get();

                final var team = game.getTeamOfPlayer(player);
                final var sharpness = gameStorage.getSharpness(team.getName());
                final var efficiency = gameStorage.getEfficiency(team.getName());
                final var bukkitItem = newItem.as(ItemStack.class);

                if (typeName.endsWith("SWORD")) {
                    bukkitItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpness);
                } else if (typeName.endsWith("BOOTS")
                        || typeName.endsWith("CHESTPLATE")
                        || typeName.endsWith("HELMET")
                        || typeName.endsWith("LEGGINGS")) {
                    shouldSell = false;
                    ShopUtil.buyArmor(player, bukkitItem.getType(), gameStorage, game);
                } else if (typeName.endsWith("PICKAXE")) {
                    bukkitItem.addUnsafeEnchantment(Enchantment.DIG_SPEED, efficiency);
                }

                newItem = ItemFactory.build(bukkitItem).orElse(newItem);
            }

            event.sellStack(materialItem);

            if (shouldSell) {
                List<Item> notFit = event.buyStack(newItem);
                if (!notFit.isEmpty()) {
                    notFit.forEach(stack -> player.getLocation().getWorld().dropItem(player.getLocation(), stack.as(ItemStack.class)));
                }
            }

            if (!MainConfig.getInstance().node("removePurchaseMessages").getBoolean(false)) {
                player.sendMessage(i18nc("buy_succes", game.getCustomPrefix()).replace("%item%", amount + "x " + getNameOrCustomNameOfItem(newItem))
                        .replace("%material%", priceAmount + " " + type.getItemName()));
            }
            Sounds.playSound(player, player.getLocation(),
                    MainConfig.getInstance().node("sounds", "item_buy").getString(), Sounds.ENTITY_ITEM_PICKUP, 1, 1);
        } else {
            if (!MainConfig.getInstance().node("removePurchaseMessages").getBoolean(false)) {
                player.sendMessage(i18nc("buy_failed", game.getCustomPrefix()).replace("%item%", amount + "x " + getNameOrCustomNameOfItem(newItem))
                        .replace("%material%", priceAmount + " " + type.getItemName()));
            }
        }
    }

    private void handleUpgrade(OnTradeEvent event) {
        event.getPlayer().sendMessage("WIP");
    }

    @Override
    public void process(InventorySetBuilder builder) {
        builder.define("team", (key, player, playerItemInfo, arguments) -> {
            GamePlayer gPlayer = Main.getPlayerGameProfile(player.as(Player.class));
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
        }).define("spawner", (key, player, playerItemInfo, arguments) -> {
            GamePlayer gPlayer = Main.getPlayerGameProfile(player.as(Player.class));
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
    }
}