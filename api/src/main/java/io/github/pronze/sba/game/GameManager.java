package io.github.pronze.sba.game;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.Optional;

public interface GameManager {

    void registerGame(@NotNull Game game);

    void unregisterGame(@NotNull Game game);

    @NotNull
    Optional<GameWrapper> getWrappedGame(@NotNull Game game);

    @NotNull
    Optional<GameWrapper> getWrappedGame(@NotNull String gameName);
}
