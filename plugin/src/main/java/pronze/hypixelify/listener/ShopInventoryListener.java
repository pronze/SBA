package pronze.hypixelify.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.PurchaseType;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToBoughtItem;
import org.screamingsandals.bedwars.api.events.BedwarsPrePropertyScanEvent;
import org.screamingsandals.bedwars.api.events.BedwarsStoreIncludeEvent;
import org.screamingsandals.bedwars.api.events.BedwarsStorePrePurchaseEvent;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.commands.DumpCommand;
import org.screamingsandals.bedwars.config.MainConfig;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.bedwars.lib.ext.configurate.ConfigurationNode;
import org.screamingsandals.bedwars.lib.lang.Message;
import org.screamingsandals.bedwars.lib.material.Item;
import org.screamingsandals.bedwars.lib.material.builder.ItemFactory;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.sgui.events.OnTradeEvent;
import org.screamingsandals.bedwars.lib.sgui.inventory.Include;
import org.screamingsandals.bedwars.lib.sgui.inventory.PlayerItemInfo;
import org.screamingsandals.bedwars.lib.utils.AdventureHelper;
import org.screamingsandals.bedwars.lib.utils.ConfigurateUtils;
import org.screamingsandals.bedwars.utils.Sounds;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBATeamUpgradePurchaseEvent;
import pronze.hypixelify.utils.ShopUtil;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.utils.Logger;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static pronze.hypixelify.lib.lang.I.i18n;

@AutoInitialize(listener = true)
public class ShopInventoryListener implements Listener {

    private final static List<String> upgradeProperties = Arrays.asList(
            "sharpness",
            "protection",
            "blindtrap",
            "healpool",
            "dragon"
    );

    @EventHandler
    public Item onPreItemProcess(BedwarsPrePropertyScanEvent event) {
        var item = event.getEvent().getStack();
        final var player = event.getEvent().getPlayer().as(Player.class);
        var game = Main.getPlayerGameProfile(player).getGame();
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
        return item;
    }

    @EventHandler
    public void onIncludeEvent(BedwarsStoreIncludeEvent event) {
        try {
            event.setCancelled(true);
            var file = event.getFile();
            var name = event.getName();
            var builder = event.getBuilder();
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

    @EventHandler
    public void onPrePurchaseEvent(BedwarsStorePrePurchaseEvent event)  {
        event.setCancelled(true);
        if (event.getType() != PurchaseType.NORMAL_ITEM) return;

        var newItem = event.getNewItem();
        var itemInfo = event.getTradeEvent().getItem();
        var player = event.getTradeEvent().getPlayer().as(Player.class);
        var game = Main.getPlayerGameProfile(player).getGame();
        var type = event.getSpawnerType();

        var materialItem = event.getMaterialItem();

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
                            newItem.getDisplayName()
                    );

                    final var itemOptional = upgradeItem(event.getTradeEvent(), propertyName);
                    newItem = itemOptional.orElse(newItem);

                    final var teamUpgradeEvent = new SBATeamUpgradePurchaseEvent(
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
                    ).orElse(null);

                    //since we are  setting the price to a different one on upgrade, we do the check again
                    if (!event.getTradeEvent().hasPlayerInInventory(materialItem)
                            && !MainConfig.getInstance().node("removePurchaseMessages").getBoolean(false)) {
                        player.sendMessage(i18n("cannot-buy"));
                        return;
                    }

                    event.getTradeEvent().sellStack(materialItem);
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

        event.getTradeEvent().sellStack(materialItem);

        if (shouldSell) {
            List<Item> notFit = event.getTradeEvent().buyStack(newItem);
            if (!notFit.isEmpty()) {
                notFit.forEach(stack -> player.getLocation().getWorld().dropItem(player.getLocation(), stack.as(ItemStack.class)));
            }
        }

        if (!MainConfig.getInstance().node("removePurchaseMessages").getBoolean(false)) {
            Message.of(LangKeys.IN_GAME_SHOP_BUY_SUCCESS)
                    .prefixOrDefault(game.getCustomPrefixComponent())
                    .placeholder("item", AdventureHelper.toComponent(newItem.getAmount() + "x " + getNameOrCustomNameOfItem(newItem)))
                    .placeholder("material", AdventureHelper.toComponent(event.getTradeEvent().getPrices().get(0) + " " + type.getItemName()))
                    .send(PlayerMapper.wrapPlayer(player));
        }
        Sounds.playSound(player, player.getLocation(),
                MainConfig.getInstance().node("sounds", "item_buy").getString(), Sounds.ENTITY_ITEM_PICKUP, 1, 1);
    }

    protected static String getNameOrCustomNameOfItem(Item item) {
        try {
            if (item.getDisplayName() != null) {
                return AdventureHelper.toLegacy(item.getDisplayName());
            }
            if (item.getLocalizedName() != null) {
                return AdventureHelper.toLegacy(item.getLocalizedName());
            }
        } catch (Throwable ignored) {
        }

        var normalItemName = item.getMaterial().getPlatformName().replace("_", " ").toLowerCase();
        var sArray = normalItemName.split(" ");
        var stringBuilder = new StringBuilder();

        for (var s : sArray) {
            stringBuilder.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return stringBuilder.toString().trim();
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
}