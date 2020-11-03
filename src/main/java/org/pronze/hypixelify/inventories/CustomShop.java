package org.pronze.hypixelify.inventories;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.api.events.ApplyPropertyToItemEvent;
import org.pronze.hypixelify.api.events.PlayerToolUpgradeEvent;
import org.pronze.hypixelify.listener.AbstractListener;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToBoughtItem;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent.Result;
import org.screamingsandals.bedwars.api.events.BedwarsUpgradeBoughtEvent;
import org.screamingsandals.bedwars.api.events.BedwarsUpgradeImprovedEvent;
import org.screamingsandals.bedwars.api.game.GameStore;
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
import org.screamingsandals.bedwars.lib.sgui.item.PlayerItemInfo;
import org.screamingsandals.bedwars.lib.sgui.utils.MapReader;
import org.screamingsandals.bedwars.utils.Debugger;
import org.screamingsandals.bedwars.utils.Sounds;

import java.io.File;
import java.util.*;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

//TODO: simplify the whole class smh, what was I thinking?

public class CustomShop extends AbstractListener {

    public static final HashMap<Integer, Integer> Prices = new HashMap<>();
    private final Map<String, SimpleInventories> shopMap = new HashMap<>();
    private final Options options = new Options(Main.getInstance());

    private final List<String> const_properties = Arrays.asList(
            "sharpness",
            "protection",
            "blindtrap",
            "healpool"
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

        options.setPrefix(SBAHypixelify.getConfigurator().config.getString("shop-name", "[SBAHypixelify] Shop"));
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

        loadNewShop("default", null, true);
    }


    public void destroy(){
        HandlerList.unregisterAll(this);
    }

    private static String getNameOrCustomNameOfItem(ItemStack stack) {
        try {
            if (stack.hasItemMeta()) {
                ItemMeta meta = stack.getItemMeta();
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

        String normalItemName = stack.getType().name().replace("_", " ").toLowerCase();
        String[] sArray = normalItemName.split(" ");
        StringBuilder stringBuilder = new StringBuilder();

        for (String s : sArray) {
            stringBuilder.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    public void show(Player player, GameStore store) {
        try {
            boolean parent = true;
            String file = null;
            if (store != null) {
                parent = store.getUseParent();
                file = store.getShopFile();
            }
            if (file != null) {
                if (file.endsWith(".yml")) {
                    file = file.substring(0, file.length() - 4);
                }
                String name = (parent ? "+" : "-") + file;
                if (!shopMap.containsKey(name)) {
                    if (Main.getConfigurator().config.getBoolean("turnOnExperimentalGroovyShop", false) && new File(Main.getInstance().getDataFolder(), file + ".groovy").exists()) {
                        loadNewShop(name, file + ".groovy", parent);
                    } else {
                        loadNewShop(name, file + ".yml", parent);
                    }
                }
                SimpleInventories shop = shopMap.get(name);
                shop.openForPlayer(player);
            } else {
                shopMap.get("default").openForPlayer(player);
            }
        } catch (Throwable ignored) {
            player.sendMessage(" Your shop.yml is invalid! Check it out or contact us on Discord");
        }
    }

    @EventHandler
    public void onGeneratingItem(GenerateItemEvent event) {
        if (!shopMap.containsValue(event.getFormat())) {
            return;
        }

        PlayerItemInfo item = event.getInfo();
        Player player = event.getPlayer();
        Game game = Main.getPlayerGameProfile(player).getGame();
        MapReader reader = item.getReader();
        if (reader.containsKey("price") && reader.containsKey("price-type")) {
            int price = reader.getInt("price");
            ItemSpawnerType type = Main.getSpawnerType((reader.getString("price-type")).toLowerCase());
            if (type == null) {
                return;
            }


            try {
                if (event.getStack() != null && event.getStack().getType().name().endsWith("SWORD")) {
                    ItemStack stack = event.getStack();
                    RunningTeam rt = game.getTeamOfPlayer(player);
                    if (rt != null && Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).getSharpness(rt.getName()) != 0) {
                        stack.addEnchantment(Enchantment.DAMAGE_ALL,
                                Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).getSharpness(rt.getName()));
                        event.setStack(stack);
                    }
                }
                if (event.getStack() != null && event.getStack().getType().name().endsWith("BOOTS")) {
                    ItemStack stack = event.getStack();
                    RunningTeam rt = game.getTeamOfPlayer(player);
                    if (rt != null && Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).getProtection(rt.getName()) != 0) {
                        stack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,
                                Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).getProtection(rt.getName()));
                        event.setStack(stack);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            //
            boolean enabled = Main.getConfigurator().config.getBoolean("lore.generate-automatically", true);
            enabled = reader.getBoolean("generate-lore", enabled);

            List<String> loreText = reader.getStringList("generated-lore-text",
                    Main.getConfigurator().config.getStringList("lore.text"));

            String nprice = Integer.toString(price);


            if (enabled) {
                ItemStack stack = event.getStack();
                ItemMeta stackMeta = stack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if (stackMeta.hasLore()) {
                    lore = stackMeta.getLore();
                }
                for (String s : loreText) {
                    s = s.replaceAll("%price%", nprice);
                    s = s.replaceAll("%resource%", type.getItemName());
                    s = s.replaceAll("%amount%", Integer.toString(stack.getAmount()));
                    assert lore != null;
                    lore.add(s);
                }
                stackMeta.setLore(lore);
                stack.setItemMeta(stackMeta);
                event.setStack(stack);
            }


            if (item.hasProperties()) {
                for (ItemProperty property : item.getProperties()) {
                    if (property.hasName()) {
                        ItemStack newItem = event.getStack();
                        ApplyPropertyToItemEvent applyEvent = new ApplyPropertyToItemEvent(game,
                                player, newItem, property.getReader(player, item).convertToMap(), reader);
                        Main.getInstance().getServer().getPluginManager().callEvent(applyEvent);
                        event.setStack(newItem);
                    }

                }
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
        if (Main.getPlayerGameProfile(event.getPlayer()).isSpectator) return;
        if (SBAHypixelify.getConfigurator().config.getBoolean("store.replace-store-with-hypixelstore", true)) {
            event.setResult(Result.DISALLOW_THIRD_PARTY_SHOP);
            this.show(event.getPlayer(), event.getStore());
        }
    }

    @EventHandler
    public void onShopTransaction(ShopTransactionEvent event) {
        if (!shopMap.containsValue(event.getFormat()) || event.isCancelled()) {
            return;
        }

        MapReader reader = event.getItem().getReader();
        if (reader.containsKey("upgrade")) {
            handleUpgrade(event);
        } else {
            handleBuy(event);
        }
    }

    @EventHandler
    public void onApplyPropertyToItem(ApplyPropertyToItemEvent event) {
        String price = null;
        org.screamingsandals.bedwars.api.game.Game game = event.getGame();
        Player player = event.getPlayer();
        RunningTeam team = game.getTeamOfPlayer(player);

        if (event.getPropertyName().equalsIgnoreCase("applycolorbyteam")
                || event.getPropertyName().equalsIgnoreCase("transform::applycolorbyteam")) {
            CurrentTeam t = (CurrentTeam) event.getGame().getTeamOfPlayer(player);

            if (Main.getConfigurator().config.getBoolean("automatic-coloring-in-shop")) {
                event.setStack(Main.applyColor(t.teamInfo.color, event.getStack()));
            }
        } else if (!event.getPropertyName().equalsIgnoreCase("sharpness")
                && !event.getPropertyName().equalsIgnoreCase("protection")) {
            return;
        } else if (event.getPropertyName().equalsIgnoreCase("sharpness")) {
            if (team == null) return;
            ItemStack stack = event.getStack();
            int level = Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).getSharpness(team.getName()) + 1;
            if (level == 5) {
                stack.removeEnchantment(Enchantment.DAMAGE_ALL);
                stack.setLore(Arrays.asList("Maximum Enchant", "Your team already has maximum Enchant."));
                stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            } else {
                stack.addEnchantment(Enchantment.DAMAGE_ALL, level);
                price = Integer.toString(Prices.get(level));
                event.setPrice(price);
                event.setStack(stack);
            }
        } else if (event.getPropertyName().equalsIgnoreCase("protection")) {
            if (team == null) return;
            ItemStack stack = event.getStack();
            int level = Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).getProtection(team.getName()) + 1;
            if (level == 5) {
                stack.removeEnchantment(Enchantment.DAMAGE_ALL);
                stack.setLore(Arrays.asList("Maximum Enchant", "Your team already has maximum Enchant."));
                stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            } else {
                stack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
                price = Integer.toString(Prices.get(level));
                event.setPrice(price);
                event.setStack(stack);
            }
        }

        if (event.getStack().getItemMeta().getLore() != null) {
            for (String st : event.getStack().getItemMeta().getLore()) {
                if (st.contains("Maximum Enchant")) return;
            }
        }

        //fix lores
        boolean enabled = Main.getConfigurator().config.getBoolean("lore.generate-automatically", true);

        if (enabled && price != null) {
            List<String> loreText = event.getReader().getStringList("generated-lore-text",
                    Main.getConfigurator().config.getStringList("lore.text"));
            ItemSpawnerType type = Main.getSpawnerType((event.getReader().getString("price-type")).toLowerCase());
            ItemStack stack = event.getStack();
            ItemMeta stackMeta = stack.getItemMeta();
            List<String> lore = new ArrayList<>();
            for (String s : loreText) {
                s = s.replaceAll("%price%", price);
                s = s.replaceAll("%resource%", type.getItemName());
                s = s.replaceAll("%amount%", Integer.toString(stack.getAmount()));
                lore.add(s);
            }
            stackMeta.setLore(lore);
            stack.setItemMeta(stackMeta);
            event.setStack(stack);
        }

    }

    private void loadNewShop(String name, String fileName, boolean useParent) {
        SimpleInventories format = new SimpleInventories(options);
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
        } catch (Exception ignored) {
            Bukkit.getLogger().severe("Wrong shop.yml configuration!");
            Bukkit.getLogger().severe("Your villagers won't work, check validity of your YAML!");
        }

        format.generateData();
        shopMap.put(name, format);
    }

    public void buystack(ItemStack newItem, ShopTransactionEvent event) {
        Player player = event.getPlayer();
        HashMap<Integer, ItemStack> noFit = player.getInventory().addItem(newItem);
        if (!noFit.isEmpty()) {
            noFit.forEach((i, stack) -> player.getLocation().getWorld().dropItem(player.getLocation(), stack));
        }
    }

    public void sellstack(ItemStack newItem, ShopTransactionEvent event) {
        Player player = event.getPlayer();
        player.getInventory().removeItem(newItem);
    }

    @EventHandler
    public void onPlayerToolUpgrade(PlayerToolUpgradeEvent e) {
        Player player = e.getPlayer();
        ItemStack newItem = e.getUpgradedItem();
        RunningTeam team = e.getTeam();
        String name = e.getName();
        org.screamingsandals.bedwars.api.game.Game game = e.getGame();

        if (name.equalsIgnoreCase("sharpness")) {
            if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "SWORD", Enchantment.DAMAGE_ALL)) {
                e.setCancelled(true);
                player.sendMessage(Messages.message_greatest_enchantment);
            } else {
                int level = newItem.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                int price = Prices.get(level);
                ItemStack materialItem = e.getStackFromPrice(price);
                if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                    Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).setSharpness(team.getName()
                            , level);
                }
                e.setPrice(Integer.toString(price));
            }
        } else if (name.equalsIgnoreCase("efficiency")) {
            if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "PICKAXE", Enchantment.DIG_SPEED)) {
                e.setCancelled(true);
                player.sendMessage(Messages.message_greatest_enchantment);
            }
        } else if (name.equalsIgnoreCase("blindtrap")) {
            if (SBAHypixelify.getGameStorage(game) == null) {
                e.setCancelled(true);
                player.sendMessage(Messages.ERROR_OCCURED);
                return;
            }
            if (Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).isTrapEnabled(team)) {
                player.sendMessage(Messages.trap_timeout_message);
                e.setCancelled(true);
            } else {
                Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).setTrap(team, true);
                team.getConnectedPlayers().forEach(pl -> sendTitle(pl, Messages.blindnessTrapPurchased, "", 20, 40, 20));
            }
        } else if (name.equalsIgnoreCase("healpool")) {
            if (SBAHypixelify.getGameStorage(game) == null) {
                e.setCancelled(true);
                player.sendMessage(Messages.ERROR_OCCURED);
                return;
            }
            if (Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).isPoolEnabled(team)) {
                player.sendMessage(Messages.trap_timeout_message);
                e.setCancelled(true);
            } else {
                Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).setPool(team, true);
                team.getConnectedPlayers().forEach(pl -> pl.sendMessage(Messages.message_purchase_heal_pool
                        .replace("{player}", player.getName())));
            }
        } else if (name.equalsIgnoreCase("protection")) {
            if (Objects.requireNonNull(player.getInventory().getBoots())
                    .getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) >= 4) {
                e.setCancelled(true);
                player.sendMessage("§c§l" + Messages.message_greatest_enchantment);
            } else {
                int level = newItem.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
                int price = Prices.get(level);
                ItemStack materialItem = e.getStackFromPrice(price);
                if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                    Objects.requireNonNull(SBAHypixelify.getGameStorage(game)).setProtection(team.getName()
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
        Player player = event.getPlayer();
        Game game = Main.getPlayerGameProfile(event.getPlayer()).getGame();
        RunningTeam team = game.getTeamOfPlayer(player);
        ClickType clickType = event.getClickType();
        MapReader mapReader = event.getItem().getReader();
        String priceType = event.getType().toLowerCase();
        ItemSpawnerType type = Main.getSpawnerType(priceType);
        ItemStack newItem = event.getStack();

        int amount = newItem.getAmount();
        int price = event.getPrice();
        int inInventory = 0;

        if (mapReader.containsKey("currency-changer")) {
            String changeItemToName = mapReader.getString("currency-changer");
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

        ItemStack materialItem = type.getStack(price);

        String propName = null;

        if (event.hasPlayerInInventory(materialItem)) {
            if (event.hasProperties()) {
                for (ItemProperty property : event.getProperties()) {
                    if (property.hasName()) {
                        if (ShopUtil.isABedwarsSpecialProperty(property.getPropertyName())) {
                            BedwarsApplyPropertyToBoughtItem applyEvent = new BedwarsApplyPropertyToBoughtItem(game, player,
                                    newItem, property.getReader(player).convertToMap());
                            Main.getInstance().getServer().getPluginManager().callEvent(applyEvent);

                            newItem = applyEvent.getStack();
                        } else {
                            ApplyPropertyToItemEvent applyEvent = new ApplyPropertyToItemEvent(game, player,
                                    newItem, property.getReader(player, event.getItem()).convertToMap(), mapReader);
                            Main.getInstance().getServer().getPluginManager().callEvent(applyEvent);
                            newItem = applyEvent.getStack();
                            if (applyEvent.getPrice() != null)
                                materialItem = type.getStack(Integer.parseInt(applyEvent.getPrice()));

                            if (const_properties.contains(property.getPropertyName().toLowerCase())) {
                                propName = property.getPropertyName();
                            }

                        }
                    }
                }
            }

            boolean shouldSellStack = true;


            if (propName != null) {
                PlayerToolUpgradeEvent e = new PlayerToolUpgradeEvent(player, newItem, propName, team, game, type);
                Bukkit.getServer().getPluginManager().callEvent(e);
                if (e.isCancelled()) {
                    return;
                }
                if (e.getPrice() != null)
                    materialItem = type.getStack(Integer.parseInt(e.getPrice()));

                //since we are  setting the price to a different one on upgrade, we do the check again
                if (!event.hasPlayerInInventory(materialItem) &&
                        !Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                    player.sendMessage(Objects.requireNonNull(SBAHypixelify.getConfigurator().config.getString("message.cannot-buy", "§cYou don't have enough {price}"))
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

            } else if (newItem.getType().name().endsWith("SWORD")) {

                if (!player.getInventory().contains(newItem.getType())) {
                    ShopUtil.upgradeSwordOnPurchase(player, newItem, game);
                    buystack(newItem, event);
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
                ShopUtil.removeAxeOrPickaxe(player, newItem);
                if (!player.getInventory().contains(newItem)) {
                    buystack(newItem, event);
                } else {
                    player.sendMessage(Messages.already_purchased_thing
                            .replace("{thing}",
                                    newItem.getType().name().substring
                                            (newItem.getType().name().indexOf("_")).substring(1)));
                    shouldSellStack = false;
                }
            } else {
                buystack(newItem, event);
            }

            if (shouldSellStack) {
                sellstack(materialItem, event);
                if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                    player.sendMessage(Objects.requireNonNull(SBAHypixelify.getConfigurator().config.getString("message.purchase", "§aYou purchased &e"))
                            .replace("{item}", getNameOrCustomNameOfItem(newItem)));
                }
                Sounds.playSound(player, player.getLocation(),
                        Main.getConfigurator().config.getString("sounds.on_item_buy"), Sounds.ENTITY_ITEM_PICKUP, 1, 1);
            }


        } else {
            if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                player.sendMessage(Objects.requireNonNull(SBAHypixelify.getConfigurator().config.getString("message.cannot-buy", "§cYou don't have enough {price}"))
                        .replace("{price}", priceType));
            }
        }
    }

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

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}