package pronze.sba.game;

import org.screamingsandals.bedwars.game.GameStore;
import pronze.sba.wrapper.PlayerWrapper;

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
