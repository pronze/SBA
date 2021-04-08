package pronze.hypixelify.api.game;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent;
import org.screamingsandals.bedwars.api.events.BedwarsTargetBlockDestroyedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import pronze.hypixelify.api.data.GamePlayerData;
import pronze.hypixelify.api.manager.ScoreboardManager;

import java.util.Optional;
import java.util.UUID;

public interface IArena {
    /**
     *
     * @return game storage
     */
    GameStorage getStorage();

    /**
     *
     * @return game object of this arena
     */
    Game getGame();

    /**
     *
     * @param event
     */
    void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent event);

    /**
     *
     * @param event
     */
    void onOver(BedwarsGameEndingEvent event);

    /**
     *
     * @return the {@link ScoreboardManager} instance that manages scoreboard activity for this arena.
     */
    ScoreboardManager getScoreboardManager();

    /**
     *
     * @param playerUUID the uuid of the player to query.
     * @return an optional {@link GamePlayerData} instance.
     */
    Optional<GamePlayerData> getPlayerData(UUID playerUUID);

    /**
     *
     * @param uuid the uuid instance of the player to register in the HashMap.
     * @param data the value of the uuid key to be used.
     */
    void putPlayerData(UUID uuid, GamePlayerData data);
}
