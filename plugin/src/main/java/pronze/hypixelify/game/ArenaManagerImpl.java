package pronze.hypixelify.game;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.api.manager.ArenaManager;
import pronze.lib.core.annotations.AutoInitialize;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AutoInitialize
public class ArenaManagerImpl implements ArenaManager {
    private final Map<String, pronze.hypixelify.api.game.Arena> arenaMap = new HashMap<>();

    @Override
    public void addArena(@NotNull pronze.hypixelify.api.game.Arena arena) {
        arenaMap.put(arena.getGame().getName(), arena);
    }

    @Override
    public void removeArena(@NotNull Game game) {
        arenaMap.remove(game.getName());
    }

    @Override
    public Optional<pronze.hypixelify.api.game.Arena> get(String gameName) {
        if (!arenaMap.containsKey(gameName)) {
            return Optional.empty();
        }
        return Optional.of(arenaMap.get(gameName));
    }

    @Override
    public Optional<GameStorage> getGameStorage(String gameName) {
        if (!arenaMap.containsKey(gameName)) {
            return Optional.empty();
        }
        return Optional.ofNullable(arenaMap.get(gameName).getStorage());
    }
}
