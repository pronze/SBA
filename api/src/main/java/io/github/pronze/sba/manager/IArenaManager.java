package io.github.pronze.sba.manager;

import io.github.pronze.sba.game.GameStorage;
import io.github.pronze.sba.game.IArena;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.Optional;

public interface IArenaManager {

    /**
     *
     * @param arena
     */
    void createArena(Game arena);

    /**
     *
     * @param game
     */
    void removeArena(@NotNull Game game);

    /**
     *
     * @param name
     * @return
     */
    Optional<IArena> get(String name);

    /**
     *
     * @param gameName
     * @return
     */
    Optional<GameStorage> getGameStorage(String gameName);
}
