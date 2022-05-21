package io.github.pronze.sba.utils.citizens;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.GameStore;

import io.github.pronze.sba.inventories.SBAStoreInventory;
import io.github.pronze.sba.utils.citizens.FakeDeathTrait.AiGoal;

public class GatherBlocks implements FakeDeathTrait.AiGoal {
    /**
     *
     */
    private final FakeDeathTrait fakeDeathTrait;

    /**
     * @param fakeDeathTrait
     */
    GatherBlocks(FakeDeathTrait fakeDeathTrait) {
        this.fakeDeathTrait = fakeDeathTrait;
    }

    @Override
    public boolean isAvailable() {
        // TODO Auto-generated method stub

        // Try locate a shop
        // Check if enough to buy Wool
        return false;
    }

    @Override
    public void doGoal() {
        // TODO Auto-generated method stub

        Player aiPlayer = (Player) this.fakeDeathTrait.getNPC().getEntity();
        Game g = Main.getInstance().getGameOfPlayer(aiPlayer);
        for (var storeapi : g.getGameStores()) {
            GameStore store = (GameStore) storeapi;

        }

        // Target the shop
        // If distance is close enough to the shop get the block
    }

    public void iterateShop(Player aiPlayer, GameStore gs) {
        if (gs.getShopFile() == null || !StringUtils.containsIgnoreCase(gs.getShopFile(), "upgrade")) {
            var storeInv = SBAStoreInventory.getInstance().iterate(gs);
            if (storeInv != null) {
                // storeInv.openInventory(SBA.getInstance().getPlayerWrapper(aiPlayer));
            }
        }
    }
}