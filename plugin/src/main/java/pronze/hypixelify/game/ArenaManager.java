package pronze.hypixelify.game;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.api.game.IArena;
import pronze.hypixelify.api.manager.IArenaManager;
import pronze.lib.core.Core;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AutoInitialize
public class ArenaManager implements IArenaManager {
    private final Map<String, IArena> arenaMap = new HashMap<>();

    public static ArenaManager getInstance() {
        return Core.getObjectFromClass(ArenaManager.class);
    }

    @Override
    public void createArena(@NotNull Game game) {
        final var gameName = game.getName();
        if (arenaMap.containsKey(gameName)) {
            throw new UnsupportedOperationException("Arena: " + gameName + " already exists!");
        }
        Logger.trace("Creating arena for game: {}", gameName);
        arenaMap.put(gameName, new Arena(game));
    }

    @Override
    public void removeArena(@NotNull Game game) {
        arenaMap.remove(game.getName());
    }

    @Override
    public Optional<IArena> get(String gameName) {
        return Optional.ofNullable(arenaMap.get(gameName));
    }

    @Override
    public Optional<GameStorage> getGameStorage(String gameName) {
        if (!arenaMap.containsKey(gameName)) {
            return Optional.empty();
        }
        return Optional.ofNullable(arenaMap.get(gameName).getStorage());
    }
}
