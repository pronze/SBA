package pronze.hypixelify.api.game;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.events.GameEndingEvent;
import org.screamingsandals.bedwars.api.events.TargetBlockDestroyedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import pronze.hypixelify.api.data.GamePlayerData;
import pronze.hypixelify.api.manager.ScoreboardManager;

import java.util.List;
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
    void onTargetBlockDestroyed(TargetBlockDestroyedEvent<org.screamingsandals.bedwars.game.Game, BedWarsPlayer, RunningTeam> event);

    /**
     *
     * @param event
     */
    void onOver(GameEndingEvent<org.screamingsandals.bedwars.game.Game, RunningTeam> event);

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

    /**
     *
     * @param player
     * @return
     */
    boolean isPlayerHidden(Player player);

    /**
     *
     * @param player
     */
    void removeHiddenPlayer(Player player);

    /**
     *
     * @param player
     */
    void addHiddenPlayer(Player player);

    /**
     *
     * @return
     */
    List<Player> getInvisiblePlayers();
}
