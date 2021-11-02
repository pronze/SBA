package io.github.pronze.sba.game;

import io.github.pronze.sba.manager.GameWrapperManager;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.wrapper.game.GameWrapper;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

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

    @Override
    public List<GameWrapper> getRegisteredGameWrappers() {
        return List.copyOf(registeredWrappers.values());
    }

    @OnPostEnable
    public void onPostEnable() {
        Runnable registrationTask = () -> {
            Main.getInstance().getGames().forEach(game -> registeredWrappers.put(game.getName(), wrapGame(game)));
        };
        if (Main.getInstance().getGames().isEmpty()) {
            Tasker.build(registrationTask).afterOneTick().start();
        } else {
            registrationTask.run();
        }
    }

    @Override
    public GameWrapper wrapGame(@NotNull Game game) {
        final var gameName = game.getName();
        if (registeredWrappers.containsKey(gameName)) {
            return registeredWrappers.get(gameName);
        }
        Logger.trace("Registering wrapper for game: {}", gameName);
        final var arena = new GameWrapperImpl(game);
        registeredWrappers.put(gameName, arena);
        return arena;
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
