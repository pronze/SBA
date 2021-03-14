package pronze.hypixelify.api.store;

import org.bukkit.entity.Player;

public interface GameStore {
    /**
     *
     * @param player
     * @param store
     */
    void show(Player player, org.screamingsandals.bedwars.game.GameStore store);
}
