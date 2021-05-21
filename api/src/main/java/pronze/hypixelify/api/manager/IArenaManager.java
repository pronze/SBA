package pronze.hypixelify.api.manager;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import pronze.hypixelify.api.game.IArena;
import pronze.hypixelify.api.game.GameStorage;

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
