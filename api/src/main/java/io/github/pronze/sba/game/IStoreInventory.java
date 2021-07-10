package io.github.pronze.sba.game;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bedwars.game.GameStore;
import io.github.pronze.sba.wrapper.PlayerWrapper;

import java.io.File;

/**
 * Represents an implementation for the StoreInventory.
 */
public interface IStoreInventory {

    /**
     * Opens the GameStore for the specified player.
     * @param player the player to be shown the store
     * @param gameStore the store to be displayed
     */
    void openForPlayer(@NotNull PlayerWrapper player, @NotNull GameStore gameStore);

    /**
     * @param name the name of the shop to load
     * @param file the file object of the shop
     * @param useParent true if to use the default shop, false otherwise
     */
    void loadNewShop(@NotNull String name, @Nullable File file, boolean useParent);
}
