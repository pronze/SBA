package io.github.pronze.sba.game;

import io.github.pronze.sba.manager.GameWrapperManager;
import io.github.pronze.sba.utils.Logger;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GameWrapperManagerImpl implements GameWrapperManager {

    public static GameWrapperManagerImpl getInstance() {
        return ServiceManager.get(GameWrapperManagerImpl.class);
    }

    private final Map<String, GameWrapper> registeredWrappers = new HashMap<>();

    public List<GameWrapper> getRegisteredArenas() {
        return List.copyOf(registeredWrappers.values());
    }

    @Override
    public GameWrapper wrapGame(@NotNull Game game) {
        final var gameName = game.getName();
        if (registeredWrappers.containsKey(gameName)) {
            return registeredWrappers.get(gameName);
        }
        Logger.trace("Creating arena for game: {}", gameName);
        final var arena = new GameWrapperImpl(game);
        registeredWrappers.put(gameName, arena);
        return arena;
    }

    @Override
    public void removeArena(@NotNull Game game) {
        Logger.trace("Removing arena for game: {}", game.getName());
        registeredWrappers.remove(game.getName());
    }

    @Override
    public Optional<GameWrapper> get(String gameName) {
        return Optional.ofNullable(registeredWrappers.get(gameName));
    }

    @Override
    public Optional<GameStorage> getGameStorage(String gameName) {
        if (!registeredWrappers.containsKey(gameName)) {
            return Optional.empty();
        }
        return Optional.of(registeredWrappers.get(gameName).getStorage());
    }
}
