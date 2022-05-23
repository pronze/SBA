package io.github.pronze.sba.utils.citizens;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.simpleinventories.inventory.Price;

import io.github.pronze.sba.inventories.SBAStoreInventory;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.citizens.FakeDeathTrait.AiGoal;
import lombok.Data;

public class GatherBlocks implements FakeDeathTrait.AiGoal {
    /**
     *
     */
    private final FakeDeathTrait fakeDeathTrait;
    private Trade tr = null;
    private GameStore gsTarget = null;
    private double distance;

    /**
     * @param fakeDeathTrait
     */
    GatherBlocks(FakeDeathTrait fakeDeathTrait) {
        this.fakeDeathTrait = fakeDeathTrait;
    }

    @Override
    public boolean isAvailable() {
        // TODO Auto-generated method stub

        if (fakeDeathTrait.blockPlace().isInNeedOfBlock()) {
            Player aiPlayer = (Player) this.fakeDeathTrait.getNPC().getEntity();
            Game g = Main.getInstance().getGameOfPlayer(aiPlayer);
            if (gsTarget == null) {
                distance = Double.MAX_VALUE;
                gsTarget = null;
                tr = null;
                if (g != null) {
                    for (var storeapi : g.getGameStores()) {
                        GameStore store = (GameStore) storeapi;
                        var distanceToShop = store.getStoreLocation().distance(aiPlayer.getLocation());
                        if (distanceToShop < distance) {
                            iterateShop(aiPlayer, store, (p, i) -> {
                                var potentialItemSpawnerType = Main.getSpawnerType(p.getCurrency());
                                if (potentialItemSpawnerType != null) {
                                    if (aiPlayer.getInventory().containsAtLeast(potentialItemSpawnerType.getStack(),
                                            p.getAmount())) {

                                        var is = i.as(ItemStack.class);
                                        if (is.getType().isBlock()) {
                                            tr = new Trade(p, is);
                                            gsTarget = store;
                                            distance = distanceToShop;

                                            Logger.trace("NPC {} Can afford this trade {} for {}",fakeDeathTrait.getNPC().getName(), tr.getIs(),
                                                    tr.getP());
                                        }
                                    }
                                }
                            });
                        }
                    }
                    if (tr == null)
                        Logger.trace("NPC {} Cannot afford the required blocks",fakeDeathTrait.getNPC().getName());
                }
            }
        }

        // Try locate a shop
        // Check if enough to buy Wool
        return gsTarget != null;
    }

    @Data
    private class Trade {
        private Price p;
        private ItemStack is;

        public Trade(Price p, ItemStack is) {
            this.p = p;
            this.is = is;
        }

    }

    @Override
    public void doGoal() {
        // TODO Auto-generated method stub

        Player aiPlayer = (Player) this.fakeDeathTrait.getNPC().getEntity();
        var distanceToShop = gsTarget.getStoreLocation().distance(aiPlayer.getLocation());
        if (distanceToShop > 3) {
            this.fakeDeathTrait.getNPC().getNavigator().setTarget(gsTarget.getStoreLocation());
            Logger.trace("NPC {} moving towards store",fakeDeathTrait.getNPC().getName());

        } else {
            var potentialItemSpawnerType = Main.getSpawnerType(tr.getP().getCurrency());
            if (potentialItemSpawnerType != null) {
                var toRemove = potentialItemSpawnerType.getStack(tr.getP().getAmount());

                aiPlayer.getInventory().remove(toRemove);
                aiPlayer.getInventory().addItem(tr.getIs());

                fakeDeathTrait.blockPlace().setInNeedOfBlock(false);

                Logger.trace("NPC {} doing trade {} for {}",fakeDeathTrait.getNPC().getName(), tr.getIs(), tr.getP());
            }

            gsTarget = null;
            tr = null;

        }
        // Target the shop
        // If distance is close enough to the shop get the block
    }

    public void iterateShop(Player aiPlayer, GameStore gs,
            BiConsumer<Price, org.screamingsandals.lib.item.Item> consumer) {
        if (gs.getShopFile() == null || !StringUtils.containsIgnoreCase(gs.getShopFile(), "upgrade")) {
            var storeInv = SBAStoreInventory.getInstance().iterate(gs);
            if (storeInv != null) {
                // storeInv.openInventory(SBA.getInstance().getPlayerWrapper(aiPlayer));
                /*
                 * InventorySet#getMainSubInventory
                 * SubInventory#getContents
                 * InventoryParent#getChildInventory (GenericItemInfo#getChildInventory; can
                 * return null)
                 * GenericItemInfo#getPrices
                 */
                var subInventory = storeInv.getMainSubInventory();
                for (var inventoryParent : subInventory.getContents()) {
                    for (var price : inventoryParent.getPrices()) {
                        var item = inventoryParent.getItem();
                        consumer.accept(price, item);
                    }
                }
            }
        }
    }
}