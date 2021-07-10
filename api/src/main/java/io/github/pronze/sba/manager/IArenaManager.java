package io.github.pronze.sba.manager;

import io.github.pronze.sba.game.IGameStorage;
import io.github.pronze.sba.game.IArena;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.Optional;

public interface IArenaManager {

    /**
     * Creates an Arena instance from the specified game.
     * @param game the game instance to register
     * @return the created Arena instance.
     */
    IArena createArena(Game game);

    /**
     * Removes an Arena from the Manager.
     * @param game the game instance to unregister
     */
    void removeArena(@NotNull Game game);

    /**
     * Gets an optional of arena by querying the specified arena name.
     * @param name the name of arena to query
     * @return an optional containing the arena instance which may or may not be empty depending on the query.
     */
    Optional<IArena> get(String name);

    /**
     * Gets an optional of game storage by querying the specified arena name.
     * @param gameName the name of arena to query
     * @return an optional containing the game storage instance which may or may not be empty depending on the query.
     */
    Optional<IGameStorage> getGameStorage(String gameName);
}
