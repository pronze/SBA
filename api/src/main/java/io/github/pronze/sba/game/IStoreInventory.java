package io.github.pronze.sba.game;

import org.screamingsandals.bedwars.game.GameStore;
import io.github.pronze.sba.wrapper.PlayerWrapper;

import java.io.File;

public interface IStoreInventory {

    void openForPlayer(PlayerWrapper player, GameStore gameStore);

    /**
     * @param name
     * @param file
     * @param useParent
     */
    void loadNewShop(String name, File file, boolean useParent);
}
