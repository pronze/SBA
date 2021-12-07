package io.github.pronze.sba.game;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.event.GameWrapperRegistrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
public final class GameManagerImpl {
    private final Map<String, GameWrapperImpl> wrappedGames = new HashMap<>();
    private final LoggerWrapper logger;
    private final SBAConfig config;

    @OnPostEnable
    public void onPostEnable() {
        Main.getInstance().getGames().forEach(this::wrapGame);
    }

    @OnPreDisable
    public void onPreDisable() {
        wrappedGames
                .values()
                .forEach(GameWrapperImpl::forceStop);
        wrappedGames.clear();
        logger.trace("Games have been force stopped!");
    }

    public Optional<GameWrapper> wrapGame(@NotNull Game game) {
        if (wrappedGames.containsKey(game.getName())) {
            throw new UnsupportedOperationException("Game has already been registered!");
        }

        if (config.isGameBlacklisted(game)) {
            return Optional.empty();
        }

        final var wrappedGame = new GameWrapperImpl((org.screamingsandals.bedwars.game.Game) game);
        logger.trace("Registered game: {}", game.getName());

        final var registerEvent = EventManager.fire(new GameWrapperRegistrationEvent(wrappedGame));
        if (registerEvent.isCancelled()) {
            logger.trace("Game registration event has been cancelled!");
            return Optional.empty();
        }

        return Optional.of(wrappedGame);
    }

    public Optional<GameWrapper> getWrappedGame(@NotNull Game game) {
        if (wrappedGames.containsKey(game.getName())) {
            return Optional.of(wrappedGames.get(game.getName()));
        }
        return wrapGame(game);
    }
}
