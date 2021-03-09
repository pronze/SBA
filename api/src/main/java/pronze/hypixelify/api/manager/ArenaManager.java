package pronze.hypixelify.api.manager;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import pronze.hypixelify.api.game.Arena;
import pronze.hypixelify.api.game.GameStorage;

import java.util.Optional;

public interface ArenaManager {

    /**
     *
     * @param arena
     */
    void addArena(Arena arena);

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
    Optional<Arena> get(String name);

    /**
     *
     * @param gameName
     * @return
     */
    Optional<GameStorage> getGameStorage(String gameName);
}
