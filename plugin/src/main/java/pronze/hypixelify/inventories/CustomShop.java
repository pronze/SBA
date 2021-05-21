package pronze.hypixelify.inventories;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.screamingsandals.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.ApplyPropertyToItemEvent;
import pronze.hypixelify.api.events.PlayerToolUpgradeEvent;
import pronze.hypixelify.game.ArenaManager;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToBoughtItem;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent.Result;
import org.screamingsandals.bedwars.api.events.BedwarsUpgradeBoughtEvent;
import org.screamingsandals.bedwars.api.events.BedwarsUpgradeImprovedEvent;
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
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;

import java.io.File;
import java.util.*;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

//TODO: Rewrite entire CustomShop

@Service
public class CustomShop implements Listener {

    public static CustomShop getInstance() {
        return ServiceManager.get(CustomShop.class);
    }

    private final Map<Integer, Integer> prices = new HashMap<>();
    private final Map<String, SimpleInventories> shopMap = new HashMap<>();
    private final Options options = new Options(Main.getInstance());

    private final List<String> const_properties = Arrays.asList(
            "sharpness",
            "protection",
            "blindtrap",
            "healpool"
    );

    public CustomShop() {
        prices.put(0, SBAHypixelify.getInstance().getConfigurator().getInt("upgrades.prices.Sharpness-Prot-I", 4));
        prices.put(1, SBAHypixelify.getInstance().getConfigurator().getInt("upgrades.prices.Sharpness-Prot-I", 4));
        prices.put(2, SBAHypixelify.getInstance().getConfigurator().getInt("upgrades.prices.Sharpness-Prot-II", 8));
        prices.put(3, SBAHypixelify.getInstance().getConfigurator().getInt("upgrades.prices.Sharpness-Prot-III", 12));
        prices.put(4, SBAHypixelify.getInstance().getConfigurator().getInt("upgrades.prices.Sharpness-Prot-IV", 16));

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

        options.setPrefix(SBAHypixelify.getInstance().getConfigurator().getString("shop-name", "[SBAHypixelify] Shop"));
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

        loadNewShop("default", "shop.yml", false);
    }

    private static String getNameOrCustomNameOfItem(ItemStack stack) {
        try {
            if (stack.hasItemMeta()) {
                final ItemMeta meta = stack.getItemMeta();
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

    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    public void show(Player player, org.screamingsandals.bedwars.api.game.GameStore store) {
        try {
            boolean parent = true;
            String file = null;
            if (store != null) {
                parent = (boolean) store.getClass().getMethod("getUseParent").invoke(store);
                file = (String) store.getClass().getMethod("getShopFile").invoke(store);
            }
            if (file != null) {
                if (file.endsWith(".yml")) {
                    file = file.substring(0, file.length() - 4);
                }
                String name = (parent ? "+" : "-") + file;
                if (!shopMap.containsKey(name)) {
                    if (Main.getConfigurator().config.getBoolean("turnOnExperimentalGroovyShop", false) && new File(SBAHypixelify.getPluginInstance().getDataFolder(), file + ".groovy").exists()) {
                        loadNewShop(name, file + ".groovy", parent);
                    } else {
                        loadNewShop(name, file + ".yml", parent);
                    }
                }
                final SimpleInventories shop = shopMap.get(name);
                shop.openForPlayer(player);
            } else {
                shopMap.get("default").openForPlayer(player);
            }
        } catch (Throwable e) {
            player.sendMessage(" Your shop.yml is invalid! Check it out or contact us on Discord");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onGeneratingItem(GenerateItemEvent event) {
        if (!shopMap.containsValue(event.getFormat())) {
            return;
        }

        final PlayerItemInfo item = event.getInfo();
        final Player player = event.getPlayer();
        final Game game = Main.getPlayerGameProfile(player).getGame();
        final MapReader reader = item.getReader();

        if (reader.containsKey("price") && reader.containsKey("price-type")) {
            final int price = reader.getInt("price");
            final ItemSpawnerType type = Main.getSpawnerType((reader.getString("price-type")).toLowerCase());
            if (type == null) {
                return;
            }

            final ItemStack eventStack = event.getStack();
            final String typeName = eventStack.getType().name();

            /*
                Add shop inventory enchants here
                Note: only visible to user
             */
            try {
                if (eventStack != null) {
                    final var gameStorage = ArenaManager.getInstance().getGameStorage(game.getName()).orElseThrow();

                    if (typeName.endsWith("SWORD")) {
                        final RunningTeam rt = game.getTeamOfPlayer(player);

                        int sharpness = 0;
                        try {
                            sharpness = gameStorage.getSharpness(rt.getName());
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }

                        if (rt != null && sharpness != 0) {
                            eventStack.addEnchantment(Enchantment.DAMAGE_ALL, sharpness);
                            event.setStack(eventStack);
                        }
                    }

                    if (typeName.endsWith("BOOTS")) {
                        final RunningTeam rt = game.getTeamOfPlayer(player);
                        int protection = 0;
                        try {
                            protection = gameStorage.getProtection(rt.getName());
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }

                        if (protection != 0)
                            eventStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protection);

                        event.setStack(eventStack);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            event.setStack(setLores(event.getStack(), reader, String.valueOf(price)));


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
        final Player player = event.getPlayer();
        if (Main.getPlayerGameProfile(player).isSpectator) return;
        if (SBAHypixelify.getInstance().getConfigurator().getBoolean("store.replace-store-with-hypixelstore", true)) {
            event.setResult(Result.DISALLOW_UNKNOWN);
            this.show(player, event.getStore());
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

    public ItemStack setLores(ItemStack stack, MapReader reader, String price){
        boolean enabled = Main.getConfigurator().config
                .getBoolean("lore.generate-automatically", true);

        if (enabled) {
            final List<String> loreText = reader.getStringList("generated-lore-text",
                    Main.getConfigurator().config.getStringList("lore.text"));
            final ItemSpawnerType type = Main.getSpawnerType((reader.getString("price-type")).toLowerCase());
            final ItemMeta stackMeta = stack.getItemMeta();
            final List<String> lore = new ArrayList<>();
            for (String s : loreText) {
                s = s.replaceAll("%price%", String.valueOf(price));
                s = s.replaceAll("%resource%", type.getItemName());
                s = s.replaceAll("%amount%", Integer.toString(stack.getAmount()));
                lore.add(s);
            }
            stackMeta.setLore(lore);
            stack.setItemMeta(stackMeta);
        }

        return stack;
    }

    @EventHandler
    public void onApplyPropertyToItem(ApplyPropertyToItemEvent event) {
        String price = null;
        final org.screamingsandals.bedwars.api.game.Game game = event.getGame();
        final Player player = event.getPlayer();
        final RunningTeam team = game.getTeamOfPlayer(player);
        final String propertyName = event.getPropertyName();
        final ItemStack itemStack = event.getStack();
        final var gameStorage = ArenaManager.getInstance().getGameStorage(game.getName()).orElseThrow();

        if (propertyName.equalsIgnoreCase("applycolorbyteam")
                || propertyName.equalsIgnoreCase("transform::applycolorbyteam")) {
            final CurrentTeam t = (CurrentTeam) event.getGame().getTeamOfPlayer(player);

            if (Main.getConfigurator().config.getBoolean("automatic-coloring-in-shop")) {
                event.setStack(Main.applyColor(t.teamInfo.color, itemStack));
            }

        }

        else if (propertyName.equalsIgnoreCase("sharpness")) {
            if (team == null) return;
            ItemStack stack = event.getStack();
            int level = gameStorage.getSharpness(team.getName()) + 1;
            if (level == 5) {
                stack.removeEnchantment(Enchantment.DAMAGE_ALL);
                stack.setLore(Arrays.asList("Maximum Enchant", "Your team already has maximum Enchant."));
                stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            } else {
                stack.addEnchantment(Enchantment.DAMAGE_ALL, level);
                price = Integer.toString(prices.get(level));
                event.setPrice(price);
                event.setStack(stack);
            }
        } else if (propertyName.equalsIgnoreCase("protection")) {
            if (team == null) return;
            ItemStack stack = event.getStack();
            int level = gameStorage.getProtection(team.getName()) + 1;
            if (level == 5) {
                stack.removeEnchantment(Enchantment.DAMAGE_ALL);
                stack.setLore(Arrays.asList("Maximum Enchant", "Your team already has maximum Enchant."));
                stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            } else {
                stack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
                price = Integer.toString(prices.get(level));
                event.setPrice(price);
                event.setStack(stack);
            }
        }

        final List<String> lore = event.getStack().getItemMeta().getLore();

        if (lore != null) {
            for (String st : lore) {
                if (st.contains("Maximum Enchant")) return;
            }
        }

        if (price != null) {
            event.setStack(setLores(event.getStack(), event.getReader(), price));
        }

    }

    private void loadNewShop(String name, String fileName, boolean useParent) {
        final SimpleInventories format = new SimpleInventories(options);
        try {
            if (useParent) {
                String shopFileName = "shop.yml";
                if (Main.isLegacy()) {
                    shopFileName = "legacy-shop.yml";
                }
                if (Main.getConfigurator().config.getBoolean("turnOnExperimentalGroovyShop", false)) {
                    shopFileName = "shop.groovy";
                }
                format.loadFromDataFolder(SBAHypixelify.getPluginInstance().getDataFolder(), shopFileName);
            }
            if (fileName != null) {
                if (Main.isLegacy()) {
                    if (fileName.equalsIgnoreCase("shop.yml"))
                        fileName = "legacy-shop.yml";
                    else if (fileName.equalsIgnoreCase("upgradeShop.yml"))
                        fileName = "legacy-upgradeShop.yml";
                }
                format.loadFromDataFolder(SBAHypixelify.getPluginInstance().getDataFolder(), fileName);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Wrong shop.yml configuration!");
            Bukkit.getLogger().severe("Your villagers won't work, check validity of your YAML!");
            e.printStackTrace();
        }

        format.generateData();
        shopMap.put(name, format);
    }

    public void buystack(ItemStack newItem, ShopTransactionEvent event) {
        final Player player = event.getPlayer();
        final HashMap<Integer, ItemStack> noFit = player.getInventory().addItem(newItem);

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
        final Player player = e.getPlayer();
        final ItemStack newItem = e.getUpgradedItem();
        final RunningTeam team = e.getTeam();
        final String name = e.getName();
        final org.screamingsandals.bedwars.api.game.Game game = e.getGame();
        final var wrappedPlayer = PlayerMapper.wrapPlayer(player);
        final var gameStorage = ArenaManager.getInstance().getGameStorage(game.getName()).orElseThrow();

        if (name.equalsIgnoreCase("sharpness")) {
            if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "SWORD", Enchantment.DAMAGE_ALL)) {
                e.setCancelled(true);
                LanguageService
                        .getInstance()
                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                        .send(wrappedPlayer);
            } else {
                int level = newItem.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                int price = prices.get(level);
                ItemStack materialItem = e.getStackFromPrice(price);
                if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                    gameStorage.setSharpness(team.getName(), level);
                }
                e.setPrice(Integer.toString(price));
            }
        }

        else if (name.equalsIgnoreCase("efficiency")) {
            if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "PICKAXE", Enchantment.DIG_SPEED)) {
                e.setCancelled(true);
                LanguageService
                        .getInstance()
                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                        .send(wrappedPlayer);
            }
        }

        else if (name.equalsIgnoreCase("blindtrap")) {
            if (gameStorage.isTrapEnabled(team)) {
                LanguageService
                        .getInstance()
                        .get(MessageKeys.WAIT_FOR_TRAP)
                        .send(wrappedPlayer);
                e.setCancelled(true);
            } else {
                gameStorage.setTrap(team, true);
                final var trapPurchasedTitle = LanguageService
                        .getInstance()
                        .get(MessageKeys.BLINDNESS_TRAP_PURCHASED_TITLE)
                        .toString();

                team.getConnectedPlayers().forEach(pl -> sendTitle(pl, trapPurchasedTitle, "", 20, 40, 20));
            }
        }

        else if (name.equalsIgnoreCase("healpool")) {
            if (gameStorage.isPoolEnabled(team)) {
                LanguageService
                        .getInstance()
                        .get(MessageKeys.WAIT_FOR_TRAP)
                        .send(wrappedPlayer);
                e.setCancelled(true);
            } else {
                gameStorage.setPool(team, true);
                final var purchasedPool = LanguageService
                        .getInstance()
                        .get(MessageKeys.PURCHASED_HEAL_POOL_MESSAGE)
                        .replace("%player%", player.getName())
                        .toString();

                team.getConnectedPlayers().forEach(pl -> pl.sendMessage(purchasedPool));
            }
        }

        else if (name.equalsIgnoreCase("protection")) {
            if (gameStorage.getProtection(team.getName()) >= 4) {
                e.setCancelled(true);
                LanguageService
                        .getInstance()
                        .get(MessageKeys.GREATEST_ENCHANTMENT)
                        .send(wrappedPlayer);
            } else {
                int level = newItem.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
                int price = prices.get(level);
                ItemStack materialItem = e.getStackFromPrice(price);
                if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                    gameStorage.setProtection(team.getName(), level);
                }
                e.setPrice(Integer.toString(price));
                ShopUtil.addEnchantsToPlayerArmor(player, newItem);
                for (Player playerCheck : team.getConnectedPlayers()) {
                    ShopUtil.addEnchantsToPlayerArmor(playerCheck, newItem);
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.UPGRADE_TEAM_PROTECTION)
                            .replace("%player%", player.getName())
                            .send(PlayerMapper.wrapPlayer(playerCheck));
                }
            }
        }
    }

    private void handleBuy(ShopTransactionEvent event) {
        final Player player = event.getPlayer();
        final Game game = Main.getPlayerGameProfile(event.getPlayer()).getGame();
        final RunningTeam team = game.getTeamOfPlayer(player);
        final ClickType clickType = event.getClickType();
        final MapReader mapReader = event.getItem().getReader();
        final String priceType = event.getType().toLowerCase();
        final ItemSpawnerType type = Main.getSpawnerType(priceType);
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
                        if (!const_properties.contains(property.getPropertyName().toLowerCase())) {
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
            final String typeName = newItem.getType().name();

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
                    player.sendMessage(Objects.requireNonNull(SBAHypixelify.getInstance().getConfigurator().getString("message.cannot-buy", "§cYou don't have enough {price}"))
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

            } else if (typeName.endsWith("SWORD")) {

                if (!player.getInventory().contains(newItem.getType())) {
                    ShopUtil.upgradeSwordOnPurchase(player, newItem, game);
                    buystack(newItem, event);
                } else {
                    shouldSellStack = false;
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.ALREADY_PURCHASED)
                            .replace("%thing%", "Sword")
                            .send(PlayerMapper.wrapPlayer(player));
                }
            } else if (player.getInventory().getBoots() != null
                    && newItem.getType().equals(player.getInventory().getBoots().getType())) {
                LanguageService
                        .getInstance()
                        .get(MessageKeys.ALREADY_PURCHASED)
                        .replace("%thing%", "Armor")
                        .send(PlayerMapper.wrapPlayer(player));
                shouldSellStack = false;
            } else if (newItem.getType().name().contains("BOOTS")) {
                ShopUtil.buyArmor(player, newItem.getType(), newItem.getType().name(), game);
            } else if (newItem.getType().name().endsWith("AXE")) {
                if (!player.getInventory().contains(newItem)) {
                    ShopUtil.removeAxeOrPickaxe(player, newItem);
                    buystack(newItem, event);
                } else {
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.ALREADY_PURCHASED)
                            .replace("%thing%",   newItem.getType().name().substring
                                    (newItem.getType().name().indexOf("_")).substring(1))
                            .send(PlayerMapper.wrapPlayer(player));
                    shouldSellStack = false;
                }
            } else {
                buystack(newItem, event);
            }

            if (shouldSellStack) {
                sellstack(materialItem, event);
                if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                    player.sendMessage(Objects.requireNonNull(SBAHypixelify.getInstance().getConfigurator().getString("message.purchase", "§aYou purchased &e"))
                            .replace("{item}", getNameOrCustomNameOfItem(newItem)));
                }
                Sounds.playSound(player, player.getLocation(),
                        Main.getConfigurator().config.getString("sounds.on_item_buy"), Sounds.ENTITY_ITEM_PICKUP, 1, 1);
            }


        } else {
            if (!Main.getConfigurator().config.getBoolean("removePurchaseMessages", false)) {
                player.sendMessage(Objects.requireNonNull(SBAHypixelify.getInstance().getConfigurator().getString("message.cannot-buy", "§cYou don't have enough {price}"))
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

}