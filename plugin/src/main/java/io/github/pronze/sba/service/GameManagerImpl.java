package io.github.pronze.sba.service;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.event.GameWrapperRegistrationEvent;
import io.github.pronze.sba.event.GameWrapperUnregisteredEvent;
import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.game.GameWrapperImpl;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import org.screamingsandals.lib.utils.logger.LoggerWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public final class GameManagerImpl implements GameManager {
    private final Map<String, GameWrapperImpl> wrappedGames = new HashMap<>();
    private final LoggerWrapper logger;
    private final SBAConfig config;

    @OnPostEnable
    public void onPostEnable() {
        Main.getInstance()
                .getGames()
                .forEach(this::registerGame);
    }

    @OnPreDisable
    public void onPreDisable() {
        wrappedGames
                .values()
                .forEach(GameWrapperImpl::destroy);
        wrappedGames.clear();
        logger.trace("Games have been force stopped!");
    }

    public void registerGame(@NotNull Game game) {
        final var gameName = game.getName();
        if (wrappedGames.containsKey(gameName)) {
            throw new UnsupportedOperationException("Game has already been registered!");
        }

        if (config.isGameBlacklisted(game)) {
            logger.trace("Game: {} has been blacklisted, skipping registration!", gameName);
            return;
        }

        final var wrappedGame = new GameWrapperImpl((org.screamingsandals.bedwars.game.Game) game);
        logger.trace("Registered game: {}", gameName);

        final var registerEvent = EventManager.fire(new GameWrapperRegistrationEvent(wrappedGame));
        if (registerEvent.isCancelled()) {
            logger.trace("Game registration event has been cancelled!");
        }
        wrappedGames.put(game.getName(), wrappedGame);
    }

    @Override
    public void unregisterGame(@NotNull Game game) {
        final var gameName = game.getName();
        if (!wrappedGames.containsKey(gameName)) {
            return;
        }

        final var wrappedGame = wrappedGames.get(gameName);
        EventManager.fire(new GameWrapperUnregisteredEvent(wrappedGame));
        logger.trace("Game: {} has been unregistered!", gameName);
        wrappedGames.remove(gameName);
    }

    @Override
    @NotNull
    public Optional<GameWrapper> getWrappedGame(@NotNull Game game) {
        return getWrappedGame(game.getName());
    }

    @Override
    @NotNull
    public Optional<GameWrapper> getWrappedGame(@NotNull String gameName) {
        return Optional.ofNullable(wrappedGames.get(gameName));
    }

    @NotNull
    @Override
    public Map<String, GameWrapper> getRegisteredGames() {
        return Map.copyOf(wrappedGames);
    }
}
