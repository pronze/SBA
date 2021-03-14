package pronze.hypixelify.store;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToItem;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.config.MainConfig;
import org.screamingsandals.bedwars.game.CurrentTeam;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.bedwars.lib.debug.Debug;
import org.screamingsandals.bedwars.lib.ext.configurate.ConfigurationNode;
import org.screamingsandals.bedwars.lib.material.Item;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.sgui.SimpleInventoriesCore;
import org.screamingsandals.bedwars.lib.sgui.builder.CategoryBuilder;
import org.screamingsandals.bedwars.lib.sgui.builder.InventorySetBuilder;
import org.screamingsandals.bedwars.lib.sgui.events.ItemRenderEvent;
import org.screamingsandals.bedwars.lib.sgui.events.OnTradeEvent;
import org.screamingsandals.bedwars.lib.sgui.events.PreClickEvent;
import org.screamingsandals.bedwars.lib.sgui.inventory.InventorySet;
import org.screamingsandals.bedwars.lib.sgui.inventory.PlayerItemInfo;
import org.screamingsandals.bedwars.lib.utils.AdventureHelper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAStoreOpenEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.screamingsandals.bedwars.lib.lang.I.i18nonly;

public abstract class AbstractStore implements pronze.hypixelify.api.store.GameStore, Listener {
    private final Map<String, InventorySet> shopMap = new HashMap<>();

    public AbstractStore() {
        SBAHypixelify.getInstance().registerListener(this);
    }

    public static File normalizeShopFile(String name) {
        if (name.split("\\.").length > 1) {
            return new File(SBAHypixelify.getInstance().getDataFolder().toString() + "/shops", name);
        }
        return new File(SBAHypixelify.getInstance().getDataFolder().toString() + "/shops", name + ".yml");
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

    public void show(Player player, GameStore store) {
        try {
            boolean parent = true;
            String fileName = null;
            if (store != null) {
                parent = store.getUseParent();
                fileName = store.getShopFile();
            }
            if (fileName != null) {
                var file = normalizeShopFile(fileName);
                var name = (parent ? "+" : "-") + file.getAbsolutePath();
                if (!shopMap.containsKey(name)) {
                    loadNewShop(name, file, parent);
                }
                PlayerMapper.wrapPlayer(player).openInventory(shopMap.get(name));
            } else {
                PlayerMapper.wrapPlayer(player).openInventory(shopMap.get("default"));
            }
        } catch (Throwable ignored) {
            player.sendMessage(i18nonly("prefix") + " Your shop.yml/shop.groovy is invalid! Check it out or contact us on Discord.");
        }
    }

    private void loadNewShop(String name, File file, boolean useParent) {
        final var shopBuilder = SimpleInventoriesCore.builder()
                .genericShop(true)
                .genericShopPriceTypeRequired(true)
                .animationsEnabled(true)
                .categoryOptions(localOptionsBuilder ->
                        localOptionsBuilder
                                .backItem(MainConfig.getInstance().readDefinedItem("shopback", "BARRIER"), itemBuilder ->
                                        itemBuilder.name(i18nonly("shop_back"))
                                )
                                .pageBackItem(MainConfig.getInstance().readDefinedItem("pageback", "ARROW"), itemBuilder ->
                                        itemBuilder.name(i18nonly("page_back"))
                                )
                                .pageForwardItem(MainConfig.getInstance().readDefinedItem("pageforward", "BARRIER"), itemBuilder ->
                                        itemBuilder.name(i18nonly("page_forward"))
                                )
                                .cosmeticItem(MainConfig.getInstance().readDefinedItem("shopcosmetic", "AIR"))
                                .rows(6)
                                .renderActualRows(6)
                                .renderOffset(9)
                                .renderHeaderStart(600)
                                .renderFooterStart(600)
                                .itemsOnRow(9)
                                .showPageNumber(false)
                                .inventoryType("CHEST")
                                .prefix(i18nonly("item_shop_name", "[BW] Shop"))
                )

                // old shop format compatibility
                .variableToProperty("upgrade", "upgrade")
                .variableToProperty("generate-lore", "generateLore")
                .variableToProperty("generated-lore-text", "generatedLoreText")
                .variableToProperty("currency-changer", "currencyChanger")

                .render(this::onGeneratingItem)
                .preClick(this::onPreAction)
                .buy(this::onShopTransaction)
                .call(categoryBuilder -> categoryBuilderCall(categoryBuilder, file, name));

        process(shopBuilder);
        final var shopInventory = shopBuilder.getInventorySet();

        try {
            shopInventory.getMainSubInventory().process();
        } catch (Exception ex) {
            Debug.warn("Wrong shop.yml/shop.groovy configuration!", true);
            Debug.warn("Check validity of your YAML/Groovy!", true);
            ex.printStackTrace();
        }

        shopMap.put(name, shopInventory);
    }

    public abstract void onGeneratingItem(ItemRenderEvent event);

    public abstract void onPreAction(PreClickEvent event);

    public abstract void onShopTransaction(OnTradeEvent event);

    public abstract void categoryBuilderCall(CategoryBuilder builder, File file, String name);

    public abstract void process(InventorySetBuilder builder);
}
