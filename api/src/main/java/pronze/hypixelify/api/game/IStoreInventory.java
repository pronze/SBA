package pronze.hypixelify.api.game;

import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.bedwars.lib.player.PlayerWrapper;
import org.screamingsandals.bedwars.lib.sgui.inventory.InventorySet;

import java.io.File;
import java.util.Optional;

public interface IStoreInventory {

    /**
     *
     * @return
     * @param key
     */
    Optional<InventorySet> getInventory(String key);

    /**
     *
     * @param player
     */
    void openForPlayer(PlayerWrapper player, GameStore gameStore);

    /**
     *
     * @param name
     * @param file
     * @param useParent
     */
    void loadNewShop(String name, File file, boolean useParent);
}
