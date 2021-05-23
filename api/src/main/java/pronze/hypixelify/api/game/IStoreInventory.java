package pronze.hypixelify.api.game;

import org.screamingsandals.bedwars.game.GameStore;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

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
