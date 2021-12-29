package io.github.pronze.sba.service;

import io.github.pronze.sba.game.GameWrapper;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.plugin.ServiceManager;

import java.util.Map;
import java.util.Optional;

public interface GameManager {

    static GameManager getInstance() {
        return ServiceManager.get(GameManager.class);
    }

    void registerGame(@NotNull Game game);

    void unregisterGame(@NotNull Game game);

    @NotNull
    Optional<GameWrapper> getWrappedGame(@NotNull Game game);

    @NotNull
    Optional<GameWrapper> getWrappedGame(@NotNull String gameName);

    @NotNull
    Map<String, GameWrapper> getRegisteredGames();
}
