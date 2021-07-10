package io.github.pronze.sba.game;
import io.github.pronze.sba.data.GamePlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.ItemSpawner;
import io.github.pronze.sba.manager.ScoreboardManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents an arena implementation.
 */
public interface IArena {

    /**
     * Gets the GameStorage of the current arena.
     * @return game storage of the current arena
     */
    @NotNull
    IGameStorage getStorage();

    /**
     * Gets the Game instance that has been linked to this Arena.
     * @return game object of this arena
     */
    @NotNull
    Game getGame();

    /**
     * Gets the scoreboard manager that manages the scoreboard visuals for this arena.
     * @return the ScoreboardManager instance that manages scoreboard activity for this arena
     */
    @NotNull
    ScoreboardManager getScoreboardManager();

    /**
     * Gets an optional that may or may not be empty depending if the player data has been registered in the arena.
     * @param playerUUID the uuid of the player to query
     * @return an optional {@link GamePlayerData} instance
     */
    Optional<GamePlayerData> getPlayerData(@NotNull UUID playerUUID);

    /**
     * Registers the player data to the arena.
     * @param uuid the uuid instance of the player to register
     * @param data the value of the uuid key to be used
     */
    void registerPlayerData(@NotNull UUID uuid, @NotNull GamePlayerData data);

    /**
     * Unregisters the player data from the arena.
     * @param uuid the uuid instance of the player to unregister
     */
    void unregisterPlayerData(@NotNull UUID uuid);

    /**
     * Gets whether the player is hidden from game players or not.
     * @param player the player instance to query
     * @return true if the player is hidden from game players, false otherwise
     */
    boolean isPlayerHidden(@NotNull Player player);

    /**
     * Removes the player from it's hidden state.
     * @param player the player instance to remove
     */
    void removeHiddenPlayer(@NotNull Player player);

    /**
     * Adds the player as a hidden player in the arena.
     * @param player the player instance to add
     */
    void addHiddenPlayer(@NotNull Player player);

    /**
     * Gets a list of players that are current invisible in the arena.
     * @return a list of players that are currently invisible in the arena
     */
    @NotNull
    List<Player> getInvisiblePlayers();

    /**
     *
     * @param itemSpawner the item spawner instance to be rotated.
     */
    void createRotatingGenerator(@NotNull ItemSpawner itemSpawner);
}
