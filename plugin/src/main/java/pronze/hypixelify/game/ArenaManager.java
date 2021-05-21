package pronze.hypixelify.game;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.api.game.IArena;
import pronze.hypixelify.api.manager.IArenaManager;
import pronze.hypixelify.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ArenaManager implements IArenaManager {

    public static ArenaManager getInstance() {
        return ServiceManager.get(ArenaManager.class);
    }

    private final Map<String, IArena> arenaMap = new HashMap<>();

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
