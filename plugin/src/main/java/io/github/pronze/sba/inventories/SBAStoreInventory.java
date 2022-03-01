package io.github.pronze.sba.inventories;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.StoreType;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.ShopUtil;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import lombok.SneakyThrows;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.simpleinventories.SimpleInventoriesCore;
import org.screamingsandals.simpleinventories.builder.InventorySetBuilder;
import org.screamingsandals.simpleinventories.events.ItemRenderEvent;
import org.screamingsandals.simpleinventories.inventory.Include;
import org.screamingsandals.simpleinventories.inventory.InventorySet;
import org.screamingsandals.simpleinventories.inventory.PlayerItemInfo;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Service(dependsOn = {
        SimpleInventoriesCore.class,
})
public class SBAStoreInventory extends AbstractStoreInventory {

    public static SBAStoreInventory getInstance() {
        return ServiceManager.get(SBAStoreInventory.class);
    }

    public SBAStoreInventory() {
        super("shop.yml");
    }

    @SneakyThrows
    private void loadDefault(InventorySet inventorySet) {
        inventorySet.getMainSubInventory().dropContents();
        inventorySet.getMainSubInventory().getWaitingQueue().add(Include.of(Path.of(SBAStoreInventory.class.getResource("/shop.yml").toURI())));
        inventorySet.getMainSubInventory().process();
    }

    @Override
    public void onPostGenerateItem(ItemRenderEvent event) {
       event.setStack(ShopUtil.applyTeamUpgradeEnchantsToItem(event.getStack(), event, StoreType.NORMAL));
    }

    @Override
    public void onPreGenerateItem(ItemRenderEvent event) {
        // do nothing here
    }

    @Override
    public Map.Entry<Boolean, Boolean> handlePurchase(Player player, AtomicReference<ItemStack> newItem, AtomicReference<Item> materialItem, PlayerItemInfo itemInfo, ItemSpawnerType type) {
        final var game = Main.getInstance().getGameOfPlayer(player);
        var gameStorage = SBA
                .getInstance()
                .getGameStorage(game)
                .orElseThrow();

        final var typeName = newItem.get().getType().name();
        final var team = game.getTeamOfPlayer(player);

        final var afterUnderscore = typeName.substring(typeName.contains("_") ? typeName.indexOf("_") + 1 : 0);
        /**
         * Apply enchants to item here according to TeamUpgrades.
         */
        switch (afterUnderscore.toLowerCase()) {
            case "sword":
                final var sharpness = gameStorage.getSharpnessLevel(team).orElseThrow();
                if (sharpness > 0 && sharpness < 5) {
                    newItem.get().addEnchantment(Enchantment.DAMAGE_ALL, sharpness);
                }

                if (SBAConfig.getInstance().node("replace-sword-on-upgrade").getBoolean(true)) {
                    Arrays.stream(player.getInventory().getContents().clone())
                            .filter(Objects::nonNull)
                            .filter(itemStack -> itemStack.getType().name().endsWith("SWORD"))
                            .filter(itemStack ->  !itemStack.isSimilar(newItem.get()))
                            .forEach(sword -> player.getInventory().removeItem(sword));
                }
                break;
            case "boots":
            case "chestplate":
            case "helmet":
            case "leggings":
                return Map.entry(ShopUtil.buyArmor(player, newItem.get().getType(), gameStorage, game), false);
            case "pickaxe":
                final var efficiency = gameStorage.getEfficiencyLevel(team).orElseThrow();
                if (efficiency > 0 && efficiency < 5) {
                    newItem.get().addEnchantment(Enchantment.DIG_SPEED, efficiency);
                }
                break;
        }

        return Map.entry(true, true);
    }

    @Override
    public @NotNull InventorySetBuilder getInventorySetBuilder() {
        return SimpleInventoriesCore
                .builder()
                .categoryOptions(localOptionsBuilder ->
                        localOptionsBuilder
                                .backItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "normal-shop", "shopback"), "BARRIER"), itemBuilder ->
                                        itemBuilder.name(LanguageService.getInstance().get(MessageKeys.SHOP_PAGE_BACK).toComponent())
                                )
                                .pageBackItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "normal-shop", "pageback"), "ARROW"), itemBuilder ->
                                        itemBuilder.name(LanguageService.getInstance().get(MessageKeys.SHOP_PAGE_BACK).toComponent())
                                )
                                .pageForwardItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "normal-shop", "pageforward"), "BARRIER"), itemBuilder ->
                                        itemBuilder.name(LanguageService.getInstance().get(MessageKeys.SHOP_PAGE_FORWARD).toComponent())
                                )
                                .cosmeticItem(SBAConfig.getInstance().readDefinedItem(SBAConfig.getInstance().node("shop", "normal-shop", "shopcosmetic"), "GRAY_STAINED_GLASS_PANE"))
                                .rows(SBAConfig.getInstance().node("shop", "normal-shop", "rows").getInt(6))
                                .renderActualRows(SBAConfig.getInstance().node("shop", "normal-shop", "render-actual-rows").getInt(6))
                                .renderOffset(SBAConfig.getInstance().node("shop", "normal-shop", "render-offset").getInt(0))
                                .renderHeaderStart(SBAConfig.getInstance().node("shop", "normal-shop", "render-header-start").getInt(9))
                                .renderFooterStart(SBAConfig.getInstance().node("shop", "normal-shop", "render-footer-start").getInt(600))
                                .itemsOnRow(SBAConfig.getInstance().node("shop", "normal-shop", "items-on-row").getInt(9))
                                .showPageNumber(SBAConfig.getInstance().node("shop", "normal-shop", "show-page-numbers").getBoolean(false))
                                .inventoryType(SBAConfig.getInstance().node("shop", "normal-shop", "inventory-type").getString("CHEST"))
                                .prefix(LanguageService.getInstance().get(MessageKeys.SHOP_NAME).toComponent())
                );
    }

    @EventHandler
    public void onBedWarsOpenShop(BedwarsOpenShopEvent event) {
        final var shopFile = event.getStore().getShopFile();
        if (shopFile ==null || (shopFile != null && shopFile.equalsIgnoreCase("shop.yml")) || event.getStore().getUseParent()) {
            if (SBAConfig.getInstance().node("shop", "normal-shop", "enabled").getBoolean()) {
                event.setResult(BedwarsOpenShopEvent.Result.DISALLOW_UNKNOWN);
                Logger.trace("Player: {} has opened store!", event.getPlayer().getName());
                openForPlayer(PlayerMapper.wrapPlayer(event.getPlayer()).as(SBAPlayerWrapper.class), (GameStore) event.getStore());
            }
        }
    }
}