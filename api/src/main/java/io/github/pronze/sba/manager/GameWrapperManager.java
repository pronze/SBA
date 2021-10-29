package io.github.pronze.sba.manager;

import io.github.pronze.sba.game.GameStorage;
import io.github.pronze.sba.game.GameWrapper;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.List;
import java.util.Optional;

public interface GameWrapperManager {

    /**
     * Creates an GameWrapper instance from the specified game.
     * @param game the game instance to register
     * @return the created GameWrapper instance.
     */
    GameWrapper wrapGame(@NotNull Game game);

    /**
     * Gets an optional of arena by querying the specified arena name.
     * @param name the name of arena to query
     * @return an optional containing the arena instance which may or may not be empty depending on the query.
     */
    Optional<GameWrapper> get(String name);

    /**
     * Gets an optional of game storage by querying the specified arena name.
     * @param gameName the name of arena to query
     * @return an optional containing the game storage instance which may or may not be empty depending on the query.
     */
    Optional<GameStorage> getGameStorage(String gameName);

    List<GameWrapper> getRegisteredGameWrappers();
}
